/** create by Guo Limin on 2021/1/31. */
package com.github.x19990416.mxpaas.admin.modules.system.rest;

import com.github.x19990416.mxpaas.admin.common.exception.BadRequestException;
import com.github.x19990416.mxpaas.admin.modules.system.domain.DictDetail;
import com.github.x19990416.mxpaas.admin.modules.system.service.DictDetailService;
import com.github.x19990416.mxpaas.admin.modules.system.service.dto.DictDetailDto;
import com.github.x19990416.mxpaas.admin.modules.system.service.dto.DictDetailQueryCriteria;
import com.google.common.collect.Maps;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@Tag(name = "系统：字典详情管理")
@RequestMapping("/api/dictDetail")
public class DictDetailController {

  private final DictDetailService dictDetailService;
  private static final String ENTITY_NAME = "dictDetail";

  @Operation(method = "查询字典详情")
  @GetMapping
  public ResponseEntity<Object> query(
      DictDetailQueryCriteria criteria,
      @PageableDefault(
              sort = {"dictSort"},
              direction = Sort.Direction.ASC)
          Pageable pageable) {
    return new ResponseEntity<>(dictDetailService.queryAll(criteria, pageable), HttpStatus.OK);
  }

  @Operation(method = "查询多个字典详情")
  @GetMapping(value = "/map")
  public ResponseEntity<Object> getDictDetailMaps(@RequestParam String dictName) {
    String[] names = dictName.split("[,，]");
    Map<String, List<DictDetailDto>> dictMap = Maps.newHashMap();
    for (String name : names) {
      dictMap.put(name, dictDetailService.getDictByName(name));
    }
    return new ResponseEntity<>(dictMap, HttpStatus.OK);
  }

  @Operation(method = "新增字典详情")
  @PostMapping
  @PreAuthorize("@el.check('dict:add')")
  public ResponseEntity<Object> create(@Validated @RequestBody DictDetail resources) {
    if (resources.getId() != null) {
      throw new BadRequestException("A new " + ENTITY_NAME + " cannot already have an ID");
    }
    dictDetailService.create(resources);
    return new ResponseEntity<>(HttpStatus.CREATED);
  }

  @Operation(method = "修改字典详情")
  @PutMapping
  @PreAuthorize("@el.check('dict:edit')")
  public ResponseEntity<Object> update(
      @Validated(DictDetail.Update.class) @RequestBody DictDetail resources) {
    dictDetailService.update(resources);
    return new ResponseEntity<>(HttpStatus.NO_CONTENT);
  }

  @Operation(method = "删除字典详情")
  @DeleteMapping(value = "/{id}")
  @PreAuthorize("@el.check('dict:del')")
  public ResponseEntity<Object> delete(@PathVariable Long id) {
    dictDetailService.delete(id);
    return new ResponseEntity<>(HttpStatus.OK);
  }
}
