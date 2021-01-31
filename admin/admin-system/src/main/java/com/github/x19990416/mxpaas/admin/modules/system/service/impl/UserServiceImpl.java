/*
 *  Copyright (c) 2020-2021 Guo Limin
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.github.x19990416.mxpaas.admin.modules.system.service.impl;

import com.github.x19990416.mxpaas.admin.common.config.FileProperties;
import com.github.x19990416.mxpaas.admin.common.exception.BadRequestException;
import com.github.x19990416.mxpaas.admin.common.exception.EntityExistException;
import com.github.x19990416.mxpaas.admin.common.exception.EntityNotFoundException;
import com.github.x19990416.mxpaas.admin.common.utils.*;
import com.github.x19990416.mxpaas.admin.modules.security.service.OnlineUserService;
import com.github.x19990416.mxpaas.admin.modules.security.service.UserCacheClean;
import com.github.x19990416.mxpaas.admin.modules.system.domain.User;
import com.github.x19990416.mxpaas.admin.modules.system.repository.UserRepository;
import com.github.x19990416.mxpaas.admin.modules.system.service.UserService;
import com.github.x19990416.mxpaas.admin.modules.system.service.dto.*;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.util.Strings;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotBlank;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@CacheConfig(cacheNames = "user")
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
  private final UserRepository userRepository;

  private final UserMapper userMapper;

  private final FileProperties properties;

  private final RedisUtil redisUtils;

  private final UserCacheClean userCacheClean;

  private final OnlineUserService onlineUserService;

  @Override
  public UserDto findById(long id) {
    return userRepository
        .findById(id)
        .map(userMapper::toDto)
        .orElseThrow(() -> new EntityNotFoundException(User.class,"id",String.valueOf(id)));
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public void create(User resources) {
    if (userRepository.findByUsername(resources.getUsername()) != null) {
      throw new EntityExistException(User.class, "username", resources.getUsername());
    }
    if (userRepository.findByEmail(resources.getEmail()) != null) {
      throw new EntityExistException(User.class, "email", resources.getEmail());
    }
    if (userRepository.findByPhone(resources.getPhone()) != null) {
      throw new EntityExistException(User.class, "phone", resources.getPhone());
    }
    userRepository.save(resources);
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public void update(User resources) throws Exception {
    User user =
        userRepository
            .findById(resources.getId())
            .orElseThrow(() -> new BadRequestException("user_id[" + resources.getId() + "]不存在"));

    User user1 = userRepository.findByUsername(resources.getUsername());
    User user2 = userRepository.findByEmail(resources.getEmail());
    User user3 = userRepository.findByPhone(resources.getPhone());

    if (user1 != null && !user1.getId().equals(user.getId())) {
      throw new EntityExistException(User.class, "username", resources.getUsername());
    }
    if (user2 != null && !user.getId().equals(user2.getId())) {
      throw new EntityExistException(User.class, "email", resources.getEmail());
    }
    if (user3 != null && !user.getId().equals(user3.getId())) {
      throw new EntityExistException(User.class, "phone", resources.getPhone());
    }

    // 如果用户的角色改变
    if (!resources.getRoles().equals(user.getRoles())) {
      redisUtils.del(CacheKey.DATA_USER + resources.getId());
      redisUtils.del(CacheKey.MENU_USER + resources.getId());
      redisUtils.del(CacheKey.ROLE_AUTH + resources.getId());
    }
    // 用户禁用
    if (!resources.getEnabled()) {
      onlineUserService.kickOutForUsername(resources.getUsername());
    }
    user.setUsername(resources.getUsername());
    user.setEmail(resources.getEmail());
    user.setEnabled(resources.getEnabled());
    user.setRoles(resources.getRoles());
    user.setDept(resources.getDept());
    user.setJobs(resources.getJobs());
    user.setPhone(resources.getPhone());
    user.setNickName(resources.getNickName());
    user.setGender(resources.getGender());
    userRepository.save(user);
    // 清除缓存
    delCaches(user.getId(), user.getUsername());
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public void delete(Set<Long> ids) {
    for (Long id : ids) {
      // 清理缓存
      UserDto user = findById(id);
      delCaches(user.getId(), user.getUsername());
    }
    userRepository.deleteAllByIdIn(ids);
  }

  @Override
  public UserDto findByName(String userName) {
    User user = userRepository.findByUsername(userName);
    if (user == null) {
      throw new EntityNotFoundException(User.class, "name", userName);
    } else {
      return userMapper.toDto(user);
    }
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public void updatePass(String username, String encryptPassword) {
    userRepository.updatePass(username, encryptPassword, new Date());
    flushCache(username);
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public Map<String, String> updateAvatar(MultipartFile multipartFile) {
    User user = userRepository.findByUsername(SecurityUtil.getCurrentUsername());
    String oldPath = user.getAvatarPath();
    File file = FileUtil.upload(multipartFile, properties.getPath().getAvatar());
    user.setAvatarPath(Objects.requireNonNull(file).getPath());
    user.setAvatarName(file.getName());
    userRepository.save(user);
    if (Strings.isNotBlank(oldPath)) {
      new File(oldPath).deleteOnExit();
    }
    @NotBlank String username = user.getUsername();
    flushCache(username);
    return new HashMap<String, String>(1) {{
      put("avatar", file.getName());
    }};
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public void updateEmail(String username, String email) {
    userRepository.updateEmail(username, email);
    flushCache(username);
  }

  @Override
  public Object queryAll(UserQueryCriteria criteria, Pageable pageable) {
    Page<User> page = userRepository.findAll((root, criteriaQuery, criteriaBuilder) -> QueryHelper.getPredicate(root, criteria, criteriaBuilder), pageable);
    return PageUtil.toPage(page.map(userMapper::toDto));
  }

  @Override
  public List<UserDto> queryAll(UserQueryCriteria criteria) {
    List<User> users = userRepository.findAll((root, criteriaQuery, criteriaBuilder) -> QueryHelper.getPredicate(root, criteria, criteriaBuilder));
    return userMapper.toDto(users);
  }

  @Override
  public void download(List<UserDto> queryAll, HttpServletResponse response) throws IOException {
    List<Map<String, Object>> list = new ArrayList<>();
    for (UserDto userDTO : queryAll) {
      List<String> roles = userDTO.getRoles().stream().map(RoleSmallDto::getName).collect(Collectors.toList());
      Map<String, Object> map = new LinkedHashMap<>();
      map.put("用户名", userDTO.getUsername());
      map.put("角色", roles);
      map.put("部门", userDTO.getDept().getName());
      map.put("岗位", userDTO.getJobs().stream().map(JobSmallDto::getName).collect(Collectors.toList()));
      map.put("邮箱", userDTO.getEmail());
      map.put("状态", userDTO.getEnabled() ? "启用" : "禁用");
      map.put("手机号码", userDTO.getPhone());
      map.put("修改密码的时间", userDTO.getPwdResetTime());
      map.put("创建日期", userDTO.getCreateTime());
      list.add(map);
    }
    FileUtil.downloadExcel(list, response);
  }


  @Override
  @Transactional(rollbackFor = Exception.class)
  public void updateCenter(User resources) {
    User user = userRepository.findById(resources.getId()).orElseGet(User::new);
    User user1 = userRepository.findByPhone(resources.getPhone());
    if (user1 != null && !user.getId().equals(user1.getId())) {
      throw new EntityExistException(User.class, "phone", resources.getPhone());
    }
    user.setNickName(resources.getNickName());
    user.setPhone(resources.getPhone());
    user.setGender(resources.getGender());
    userRepository.save(user);
    // 清理缓存
    delCaches(user.getId(), user.getUsername());
  }

  public void delCaches(Long id, String username) {
    redisUtils.del(CacheKey.USER_ID + id);
    flushCache(username);
  }

  private void flushCache(String username) {
    userCacheClean.cleanUserCache(username);
  }
}
