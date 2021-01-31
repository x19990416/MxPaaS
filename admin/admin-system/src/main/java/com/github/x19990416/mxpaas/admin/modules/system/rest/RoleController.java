/** create by Guo Limin on 2021/1/31. */
package com.github.x19990416.mxpaas.admin.modules.system.rest;

import com.github.x19990416.mxpaas.admin.common.exception.BadRequestException;
import com.github.x19990416.mxpaas.admin.common.utils.SecurityUtil;
import com.github.x19990416.mxpaas.admin.modules.system.domain.Role;
import com.github.x19990416.mxpaas.admin.modules.system.service.RoleService;
import com.github.x19990416.mxpaas.admin.modules.system.service.dto.RoleDto;
import com.github.x19990416.mxpaas.admin.modules.system.service.dto.RoleQueryCriteria;
import com.github.x19990416.mxpaas.admin.modules.system.service.dto.RoleSmallDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@Tag(name = "系统：角色管理")
@RequestMapping("/api/roles")
public class RoleController {

  private final RoleService roleService;

  private static final String ENTITY_NAME = "role";

  @Operation(method = "获取单个role")
  @GetMapping(value = "/{id}")
  @PreAuthorize("@_sys.check('roles:list')")
  public ResponseEntity<Object> query(@PathVariable Long id) {
    return new ResponseEntity<>(roleService.findById(id), HttpStatus.OK);
  }

  @Operation(method = "导出角色数据")
  @GetMapping(value = "/download")
  @PreAuthorize("@_sys.check('role:list')")
  public void download(HttpServletResponse response, RoleQueryCriteria criteria)
      throws IOException {
    roleService.download(roleService.queryAll(criteria), response);
  }

  @Operation(method = "返回全部的角色")
  @GetMapping(value = "/all")
  @PreAuthorize("@_sys.check('roles:list','user:add','user:edit')")
  public ResponseEntity<Object> query() {
    return new ResponseEntity<>(roleService.queryAll(), HttpStatus.OK);
  }

  @Operation(method = "查询角色")
  @GetMapping
  @PreAuthorize("@_sys.check('roles:list')")
  public ResponseEntity<Object> query(RoleQueryCriteria criteria, Pageable pageable) {
    return new ResponseEntity<>(roleService.queryAll(criteria, pageable), HttpStatus.OK);
  }

  @Operation(method = "获取用户级别")
  @GetMapping(value = "/level")
  public ResponseEntity<Object> getLevel() {
    return new ResponseEntity<>(Collections.singletonMap("level", getLevels(null)), HttpStatus.OK);
  }

  @Operation(method = "新增角色")
  @PostMapping
  @PreAuthorize("@_sys.check('roles:add')")
  public ResponseEntity<Object> create(@Validated @RequestBody Role resources) {
    if (resources.getId() != null) {
      throw new BadRequestException("A new " + ENTITY_NAME + " cannot already have an ID");
    }
    getLevels(resources.getLevel());
    roleService.create(resources);
    return new ResponseEntity<>(HttpStatus.CREATED);
  }

  @Operation(method = "修改角色")
  @PutMapping
  @PreAuthorize("@_sys.check('roles:edit')")
  public ResponseEntity<Object> update(@Validated(Role.Update.class) @RequestBody Role resources) {
    getLevels(resources.getLevel());
    roleService.update(resources);
    return new ResponseEntity<>(HttpStatus.NO_CONTENT);
  }

  @Operation(method = "修改角色菜单")
  @PutMapping(value = "/menu")
  @PreAuthorize("@_sys.check('roles:edit')")
  public ResponseEntity<Object> updateMenu(@RequestBody Role resources) {
    RoleDto role = roleService.findById(resources.getId());
    getLevels(role.getLevel());
    roleService.updateMenu(resources, role);
    return new ResponseEntity<>(HttpStatus.NO_CONTENT);
  }

  @Operation(method = "删除角色")
  @DeleteMapping
  @PreAuthorize("@_sys.check('roles:del')")
  public ResponseEntity<Object> delete(@RequestBody Set<Long> ids) {
    for (Long id : ids) {
      RoleDto role = roleService.findById(id);
      getLevels(role.getLevel());
    }
    // 验证是否被用户关联
    roleService.verification(ids);
    roleService.delete(ids);
    return new ResponseEntity<>(HttpStatus.OK);
  }

  /**
   * 获取用户的角色级别
   *
   * @return /
   */
  private int getLevels(Integer level) {
    List<Integer> levels =
        roleService.findByUsersId(SecurityUtil.getCurrentUserId()).stream()
            .map(RoleSmallDto::getLevel)
            .collect(Collectors.toList());
    int min = Collections.min(levels);
    if (level != null) {
      if (level < min) {
        throw new BadRequestException("权限不足，你的角色级别：" + min + "，低于操作的角色级别：" + level);
      }
    }
    return min;
  }
}
