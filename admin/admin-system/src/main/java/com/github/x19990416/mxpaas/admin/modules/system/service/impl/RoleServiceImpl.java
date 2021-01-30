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

import com.github.x19990416.mxpaas.admin.common.exception.BadRequestException;
import com.github.x19990416.mxpaas.admin.common.exception.EntityExistException;
import com.github.x19990416.mxpaas.admin.common.utils.*;
import com.github.x19990416.mxpaas.admin.modules.security.service.UserCacheClean;
import com.github.x19990416.mxpaas.admin.modules.system.domain.Menu;
import com.github.x19990416.mxpaas.admin.modules.system.domain.Role;
import com.github.x19990416.mxpaas.admin.modules.system.domain.User;
import com.github.x19990416.mxpaas.admin.modules.system.repository.RoleRepository;
import com.github.x19990416.mxpaas.admin.modules.system.repository.UserRepository;
import com.github.x19990416.mxpaas.admin.modules.system.service.RoleService;
import com.github.x19990416.mxpaas.admin.modules.system.service.dto.*;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.util.Strings;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {
  private final RoleMapper roleMapper;
  private final RoleRepository roleRepository;
  private final RoleSmallMapper roleSmallMapper;
  private final RedisUtil redisUtils;
  private final UserRepository userRepository;
  private final UserCacheClean userCacheClean;

  @Override
  public List<RoleDto> queryAll() {
    Sort sort = Sort.by(Sort.Direction.ASC, "level");
    return roleMapper.toDto(roleRepository.findAll(sort));
  }

  @Cacheable(key = "'id:' + #p0")
  @Transactional(rollbackFor = Exception.class)
  public RoleDto findById(long id) {
    return roleMapper.toDto(
        roleRepository.findById(id).orElseThrow(() -> new BadRequestException("role_id["+id+"]权限不存在")));
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public void create(Role resources) {
    if (roleRepository.findByName(resources.getName()) != null) {
      throw new EntityExistException(Role.class, "username", resources.getName());
    }
    roleRepository.save(resources);
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public void update(final Role resources) {
    roleRepository
        .findById(resources.getId())
        .ifPresent(
            role -> {
              Role role1 = roleRepository.findByName(resources.getName());
              if (role1 != null && role1.getId().equals(role.getId()))
                throw new EntityExistException(Role.class, "username", resources.getName());
              role.setName(resources.getName());
              role.setDescription(resources.getDescription());
              role.setDataScope(resources.getDataScope());
              role.setDepts(resources.getDepts());
              role.setLevel(resources.getLevel());
              roleRepository.save(role);
              // 更新相关缓存
              delCaches(role.getId(), null);
            });
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public void delete(Set<Long> ids) {
    ids.forEach(
        id -> {
          delCaches(id, null);
        });
    roleRepository.deleteAllByIdIn(ids);
  }

  @Override
  public List<RoleSmallDto> findByUsersId(Long id) {
    return roleSmallMapper.toDto(Lists.newArrayList(roleRepository.findByUserId(id)));
  }

  @Override
  public Integer findByRoles(Set<Role> roles) {
    if (roles.size() == 0) {
      return Integer.MAX_VALUE;
    }
    Set<RoleDto> roleDtos = Sets.newConcurrentHashSet();
    for (Role role : roles) {
      roleDtos.add(findById(role.getId()));
    }
    return Collections.min(roleDtos.stream().map(RoleDto::getLevel).collect(Collectors.toList()));
  }

  @Override
  public void updateMenu(Role resources, RoleDto roleDTO) {
    Role role = roleMapper.toEntity(roleDTO);
    List<User> users = userRepository.findByRoleId(role.getId());
    // 更新菜单
    role.setMenus(resources.getMenus());
    delCaches(resources.getId(), users);
    roleRepository.save(role);
  }

  @Override
  public void untiedMenu(Long menuId) {
    roleRepository.untiedMenu(menuId);
  }

  @Override
  public Object queryAll(RoleQueryCriteria criteria, Pageable pageable) {
    Page<Role> page =
        roleRepository.findAll(
            (root, criteriaQuery, criteriaBuilder) ->
                QueryHelper.getPredicate(root, criteria, criteriaBuilder),
            pageable);
    return PageUtil.toPage(page.map(roleMapper::toDto));
  }

  @Override
  public List<RoleDto> queryAll(RoleQueryCriteria criteria) {
    List<Role> roles =
        roleRepository.findAll(
            (root, criteriaQuery, criteriaBuilder) ->
                QueryHelper.getPredicate(root, criteria, criteriaBuilder));
    return roleMapper.toDto(roles);
  }

  /** export to excel */
  @Override
  public void download(List<RoleDto> roles, HttpServletResponse response) throws IOException {
    List<Map<String, Object>> list = new ArrayList<>();
    for (RoleDto role : roles) {
      Map<String, Object> map = new LinkedHashMap<>();
      map.put("角色名称", role.getName());
      map.put("角色级别", role.getLevel());
      map.put("描述", role.getDescription());
      map.put("创建日期", role.getCreateTime());
      list.add(map);
    }
    FileUtil.downloadExcel(list, response);
  }

  @Override
  @Cacheable(key = "'auth:' + #p0.id")
  public List<GrantedAuthority> mapToGrantedAuthorities(UserDto user) {
    Set<String> permissions = new HashSet<>();
    // 如果是管理员直接返回
    if (user.getIsAdmin()) {
      permissions.add("admin");
      return permissions.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList());
    }
    Set<Role> roles = roleRepository.findByUserId(user.getId());
    permissions =
        roles.stream()
            .flatMap(role -> role.getMenus().stream())
            .filter(menu -> Strings.isNotBlank(menu.getPermission()))
            .map(Menu::getPermission)
            .collect(Collectors.toSet());
    return permissions.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList());
  }

  @Override
  public void verification(Set<Long> ids) {
    if (userRepository.countByRoles(ids) > 0) {
      throw new BadRequestException("所选角色存在用户关联，请解除关联再试！");
    }
  }

  @Override
  public List<Role> findInMenuId(List<Long> menuIds) {
      return roleRepository.findInMenuId(menuIds);

  }

  public void delCaches(Long id, List<User> users) {
    users = CollectionUtils.isEmpty(users) ? userRepository.findByRoleId(id) : users;
    if (!CollectionUtils.isEmpty(users)) {
      users.forEach(item -> userCacheClean.cleanUserCache(item.getUsername()));
      Set<Long> userIds = users.stream().map(User::getId).collect(Collectors.toSet());
      redisUtils.delByKeys(CacheKey.DATA_USER, userIds);
      redisUtils.delByKeys(CacheKey.MENU_USER, userIds);
      redisUtils.delByKeys(CacheKey.ROLE_AUTH, userIds);
    }
    redisUtils.del(CacheKey.ROLE_ID + id);
  }
}
