/** create by Guo Limin on 2021/1/31. */
package com.github.x19990416.mxpaas.admin.modules.system.rest;

import com.github.x19990416.mxpaas.admin.common.exception.BadRequestException;
import com.github.x19990416.mxpaas.admin.common.utils.PageUtil;
import com.github.x19990416.mxpaas.admin.modules.system.domain.Dept;
import com.github.x19990416.mxpaas.admin.modules.system.service.DeptService;
import com.github.x19990416.mxpaas.admin.modules.system.service.dto.DeptDto;
import com.github.x19990416.mxpaas.admin.modules.system.service.dto.DeptQueryCriteria;
import io.swagger.annotations.ApiOperation;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.*;

@RestController
@RequiredArgsConstructor
@Tag(name = "系统：部门管理")
@RequestMapping("/api/dept")
public class DeptController {

  private final DeptService deptService;
  private static final String ENTITY_NAME = "dept";

  @Operation(method = "导出部门数据")
  @GetMapping(value = "/download")
  @PreAuthorize("@_sys.check('dept:list')")
  public void download(HttpServletResponse response, DeptQueryCriteria criteria) throws Exception {
    deptService.download(deptService.queryAll(criteria, false), response);
  }

  @Operation(method = "查询部门")
  @GetMapping
  @PreAuthorize("@_sys.check('user:list','dept:list')")
  public ResponseEntity<Map> query(DeptQueryCriteria criteria) throws Exception {
    List<DeptDto> deptDtos = deptService.queryAll(criteria, true);
    return new ResponseEntity(PageUtil.toPage(deptDtos, deptDtos.size()), HttpStatus.OK);
  }

  @Operation(method = "查询部门:根据ID获取同级与上级数据")
  @PostMapping("/superior")
  @PreAuthorize("@_sys.check('user:list','dept:list')")
  public ResponseEntity<Object> getSuperior(@RequestBody List<Long> ids) {
    Set<DeptDto> deptDtos = new LinkedHashSet<>();
    for (Long id : ids) {
      DeptDto deptDto = deptService.findById(id);
      List<DeptDto> depts = deptService.getSuperior(deptDto, new ArrayList<>());
      deptDtos.addAll(depts);
    }
    return new ResponseEntity<>(deptService.buildTree(new ArrayList<>(deptDtos)), HttpStatus.OK);
  }

  @Operation(method = "新增部门")
  @PostMapping
  @PreAuthorize("@_sys.check('dept:add')")
  public ResponseEntity<Object> create(@Validated @RequestBody Dept resources) {
    if (resources.getId() != null) {
      throw new BadRequestException("A new " + ENTITY_NAME + " cannot already have an ID");
    }
    deptService.create(resources);
    return new ResponseEntity<>(HttpStatus.CREATED);
  }

  @Operation(method = "修改部门")
  @PutMapping
  @PreAuthorize("@_sys.check('dept:edit')")
  public ResponseEntity<Object> update(@Validated(Dept.Update.class) @RequestBody Dept resources) {
    deptService.update(resources);
    return new ResponseEntity<>(HttpStatus.NO_CONTENT);
  }

  @ApiOperation("删除部门")
  @DeleteMapping
  @PreAuthorize("@el.check('dept:del')")
  public ResponseEntity<Object> delete(@RequestBody Set<Long> ids) {
    Set<DeptDto> deptDtos = new HashSet<>();
    for (Long id : ids) {
      List<Dept> deptList = deptService.findByPid(id);
      deptDtos.add(deptService.findById(id));
      if (CollectionUtils.isNotEmpty(deptList)) {
        deptDtos = deptService.getDeleteDepts(deptList, deptDtos);
      }
    }
    // 验证是否被角色或用户关联
    deptService.verification(deptDtos);
    deptService.delete(deptDtos);
    return new ResponseEntity<>(HttpStatus.OK);
  }
}
