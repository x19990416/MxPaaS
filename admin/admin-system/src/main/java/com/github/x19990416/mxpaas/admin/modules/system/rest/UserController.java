/** create by Guo Limin on 2021/1/31. */
package com.github.x19990416.mxpaas.admin.modules.system.rest;

import com.github.x19990416.mxpaas.admin.common.config.RsaProperties;
import com.github.x19990416.mxpaas.admin.common.exception.BadRequestException;
import com.github.x19990416.mxpaas.admin.common.utils.PageUtil;
import com.github.x19990416.mxpaas.admin.common.utils.RsaUtil;
import com.github.x19990416.mxpaas.admin.common.utils.SecurityUtil;
import com.github.x19990416.mxpaas.admin.common.utils.enums.CodeEnum;
import com.github.x19990416.mxpaas.admin.modules.system.domain.User;
import com.github.x19990416.mxpaas.admin.modules.system.domain.vo.UserPwdVo;
import com.github.x19990416.mxpaas.admin.modules.system.service.*;
import com.github.x19990416.mxpaas.admin.modules.system.service.dto.RoleSmallDto;
import com.github.x19990416.mxpaas.admin.modules.system.service.dto.UserDto;
import com.github.x19990416.mxpaas.admin.modules.system.service.dto.UserQueryCriteria;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Tag(name = "系统：用户管理")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

  private final PasswordEncoder passwordEncoder;
  private final UserService userService;
  private final DataService dataService;
  private final DeptService deptService;
  private final RoleService roleService;
  private final VerifyService verificationCodeService;

  @Operation(method = "导出用户数据")
  @GetMapping(value = "/download")
  @PreAuthorize("@_sys.check('user:list')")
  public void download(HttpServletResponse response, UserQueryCriteria criteria)
      throws IOException {
    userService.download(userService.queryAll(criteria), response);
  }

  @Operation(method = "查询用户")
  @GetMapping
  @PreAuthorize("@_sys.check('user:list')")
  public ResponseEntity<Object> query(UserQueryCriteria criteria, Pageable pageable) {
    if (!ObjectUtils.isEmpty(criteria.getDeptId())) {
      criteria.getDeptIds().add(criteria.getDeptId());
      criteria
          .getDeptIds()
          .addAll(deptService.getDeptChildren(deptService.findByPid(criteria.getDeptId())));
    }
    // 数据权限
    List<Long> dataScopes =
        dataService.getDeptIds(userService.findByName(SecurityUtil.getCurrentUsername()));
    // criteria.getDeptIds() 不为空并且数据权限不为空则取交集
    if (!CollectionUtils.isEmpty(criteria.getDeptIds()) && !CollectionUtils.isEmpty(dataScopes)) {
      // 取交集
      criteria.getDeptIds().retainAll(dataScopes);
      if (!CollectionUtils.isEmpty(criteria.getDeptIds())) {
        return new ResponseEntity<>(userService.queryAll(criteria, pageable), HttpStatus.OK);
      }
    } else {
      // 否则取并集
      criteria.getDeptIds().addAll(dataScopes);
      return new ResponseEntity<>(userService.queryAll(criteria, pageable), HttpStatus.OK);
    }
    return new ResponseEntity<>(PageUtil.toPage(null, 0), HttpStatus.OK);
  }

  @Operation(method = "新增用户")
  @PostMapping
  @PreAuthorize("@el.check('user:add')")
  public ResponseEntity<Object> create(@Validated @RequestBody User resources) {
    checkLevel(resources);
    // 默认密码 123456
    resources.setPassword(passwordEncoder.encode("123456"));
    userService.create(resources);
    return new ResponseEntity<>(HttpStatus.CREATED);
  }

  @Operation(method = "修改用户")
  @PutMapping
  @PreAuthorize("@el.check('user:edit')")
  public ResponseEntity<Object> update(@Validated(User.Update.class) @RequestBody User resources)
      throws Exception {
    checkLevel(resources);
    userService.update(resources);
    return new ResponseEntity<>(HttpStatus.NO_CONTENT);
  }

  @Operation(method = "修改用户：个人中心")
  @PutMapping(value = "center")
  public ResponseEntity<Object> center(@Validated(User.Update.class) @RequestBody User resources) {
    if (!resources.getId().equals(SecurityUtil.getCurrentUserId())) {
      throw new BadRequestException("不能修改他人资料");
    }
    userService.updateCenter(resources);
    return new ResponseEntity<>(HttpStatus.NO_CONTENT);
  }

  @Operation(method = "删除用户")
  @DeleteMapping
  @PreAuthorize("@_sys.check('user:del')")
  public ResponseEntity<Object> delete(@RequestBody Set<Long> ids) {
    for (Long id : ids) {
      Integer currentLevel =
          Collections.min(
              roleService.findByUsersId(SecurityUtil.getCurrentUserId()).stream()
                  .map(RoleSmallDto::getLevel)
                  .collect(Collectors.toList()));
      Integer optLevel =
          Collections.min(
              roleService.findByUsersId(id).stream()
                  .map(RoleSmallDto::getLevel)
                  .collect(Collectors.toList()));
      if (currentLevel > optLevel) {
        throw new BadRequestException("角色权限不足，不能删除：" + userService.findById(id).getUsername());
      }
    }
    userService.delete(ids);
    return new ResponseEntity<>(HttpStatus.OK);
  }

  @Operation(method = "修改密码")
  @PostMapping(value = "/updatePass")
  public ResponseEntity<Object> updatePass(@RequestBody UserPwdVo passVo) throws Exception {
    String oldPass = RsaUtil.decryptByPrivateKey(RsaProperties.privateKey, passVo.getOldPass());
    String newPass = RsaUtil.decryptByPrivateKey(RsaProperties.privateKey, passVo.getNewPass());
    UserDto user = userService.findByName(SecurityUtil.getCurrentUsername());
    if (!passwordEncoder.matches(oldPass, user.getPassword())) {
      throw new BadRequestException("修改失败，旧密码错误");
    }
    if (passwordEncoder.matches(newPass, user.getPassword())) {
      throw new BadRequestException("新密码不能与旧密码相同");
    }
    userService.updatePass(user.getUsername(), passwordEncoder.encode(newPass));
    return new ResponseEntity<>(HttpStatus.OK);
  }

  @Operation(method = "修改头像")
  @PostMapping(value = "/updateAvatar")
  public ResponseEntity<Object> updateAvatar(@RequestParam MultipartFile avatar) {
    return new ResponseEntity<>(userService.updateAvatar(avatar), HttpStatus.OK);
  }

  @Operation(method = "修改邮箱")
  @PostMapping(value = "/updateEmail/{code}")
  public ResponseEntity<Object> updateEmail(@PathVariable String code, @RequestBody User user)
      throws Exception {
    String password = RsaUtil.decryptByPrivateKey(RsaProperties.privateKey, user.getPassword());
    UserDto userDto = userService.findByName(SecurityUtil.getCurrentUsername());
    if (!passwordEncoder.matches(password, userDto.getPassword())) {
      throw new BadRequestException("密码错误");
    }
    verificationCodeService.validated(
        CodeEnum.EMAIL_RESET_EMAIL_CODE.getKey() + user.getEmail(), code);
    userService.updateEmail(userDto.getUsername(), user.getEmail());
    return new ResponseEntity<>(HttpStatus.OK);
  }

  /**
   * 如果当前用户的角色级别低于创建用户的角色级别，则抛出权限不足的错误
   *
   * @param resources /
   */
  private void checkLevel(User resources) {
    Integer currentLevel =
        Collections.min(
            roleService.findByUsersId(SecurityUtil.getCurrentUserId()).stream()
                .map(RoleSmallDto::getLevel)
                .collect(Collectors.toList()));
    Integer optLevel = roleService.findByRoles(resources.getRoles());
    if (currentLevel > optLevel) {
      throw new BadRequestException("角色权限不足");
    }
  }
}
