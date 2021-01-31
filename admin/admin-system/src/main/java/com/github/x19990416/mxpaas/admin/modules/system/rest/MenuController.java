/** create by Guo Limin on 2021/1/31. */
package com.github.x19990416.mxpaas.admin.modules.system.rest;

import com.github.x19990416.mxpaas.admin.common.exception.BadRequestException;
import com.github.x19990416.mxpaas.admin.common.utils.PageUtil;
import com.github.x19990416.mxpaas.admin.common.utils.SecurityUtil;
import com.github.x19990416.mxpaas.admin.modules.system.domain.Menu;
import com.github.x19990416.mxpaas.admin.modules.system.domain.vo.MenuVo;
import com.github.x19990416.mxpaas.admin.modules.system.service.MenuService;
import com.github.x19990416.mxpaas.admin.modules.system.service.dto.MenuDto;
import com.github.x19990416.mxpaas.admin.modules.system.service.dto.MenuMapper;
import com.github.x19990416.mxpaas.admin.modules.system.service.dto.MenuQueryCriteria;
import com.google.common.collect.Sets;
import io.swagger.annotations.ApiOperation;
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
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@Tag(name = "系统：菜单管理")
@RequestMapping("/api/menus")
public class MenuController {

  private final MenuService menuService;
  private final MenuMapper menuMapper;
  private static final String ENTITY_NAME = "menu";

  @ApiOperation("导出菜单数据")
  @GetMapping(value = "/download")
  @PreAuthorize("@_sys.check('menu:list')")
  public void download(HttpServletResponse response, MenuQueryCriteria criteria) throws Exception {
    menuService.download(menuService.queryAll(criteria, false), response);
  }

  @GetMapping(value = "/build")
  @ApiOperation("获取前端所需菜单")
  public ResponseEntity<MenuVo> buildMenus() {
    List<MenuDto> menuDtoList = menuService.findByUser(SecurityUtil.getCurrentUserId());
    List<MenuDto> menuDtos = menuService.buildTree(menuDtoList);
    return new ResponseEntity(menuService.buildMenus(menuDtos), HttpStatus.OK);
  }

  @ApiOperation("返回全部的菜单")
  @GetMapping(value = "/lazy")
  @PreAuthorize("@_sys.check('menu:list','roles:list')")
  public ResponseEntity<Object> query(@RequestParam Long pid) {
    return new ResponseEntity<>(menuService.getMenus(pid), HttpStatus.OK);
  }

  @ApiOperation("根据菜单ID返回所有子节点ID，包含自身ID")
  @GetMapping(value = "/child")
  @PreAuthorize("@_sys.check('menu:list','roles:list')")
  public ResponseEntity<Long> child(@RequestParam Long id) {
    Set<Menu> menuSet = Sets.newHashSet();
    List<MenuDto> menuList = menuService.getMenus(id);
    menuSet.add(menuService.findOne(id));
    menuSet = menuService.getChildMenus(menuMapper.toEntity(menuList), menuSet);
    Set<Long> ids = menuSet.stream().map(Menu::getId).collect(Collectors.toSet());
    return new ResponseEntity(ids, HttpStatus.OK);
  }

  @GetMapping
  @ApiOperation("查询菜单")
  @PreAuthorize("@_sys.check('menu:list')")
  public ResponseEntity<Map> query(MenuQueryCriteria criteria) throws Exception {
    List<MenuDto> menuDtoList = menuService.queryAll(criteria, true);
    return new ResponseEntity(PageUtil.toPage(menuDtoList, menuDtoList.size()), HttpStatus.OK);
  }

  @ApiOperation("查询菜单:根据ID获取同级与上级数据")
  @PostMapping("/superior")
  @PreAuthorize("@_sys.check('menu:list')")
  public ResponseEntity<Object> getSuperior(@RequestBody List<Long> ids) {
    Set<MenuDto> menuDtos = new LinkedHashSet<>();
    if (CollectionUtils.isNotEmpty(ids)) {
      for (Long id : ids) {
        MenuDto menuDto = menuService.findById(id);
        menuDtos.addAll(menuService.getSuperior(menuDto, new ArrayList<>()));
      }
      return new ResponseEntity<>(menuService.buildTree(new ArrayList<>(menuDtos)), HttpStatus.OK);
    }
    return new ResponseEntity<>(menuService.getMenus(null), HttpStatus.OK);
  }

  @ApiOperation("新增菜单")
  @PostMapping
  @PreAuthorize("@_sys.check('menu:add')")
  public ResponseEntity<Object> create(@Validated @RequestBody Menu resources) {
    if (resources.getId() != null) {
      throw new BadRequestException("A new " + ENTITY_NAME + " cannot already have an ID");
    }
    menuService.create(resources);
    return new ResponseEntity<>(HttpStatus.CREATED);
  }

  @ApiOperation("修改菜单")
  @PutMapping
  @PreAuthorize("@_sys.check('menu:edit')")
  public ResponseEntity<Object> update(@Validated(Menu.Update.class) @RequestBody Menu resources) {
    menuService.update(resources);
    return new ResponseEntity<>(HttpStatus.NO_CONTENT);
  }

  @ApiOperation("删除菜单")
  @DeleteMapping
  @PreAuthorize("@_sys.check('menu:del')")
  public ResponseEntity<Object> delete(@RequestBody Set<Long> ids) {
    Set<Menu> menuSet = new HashSet<>();
    for (Long id : ids) {
      List<MenuDto> menuList = menuService.getMenus(id);
      menuSet.add(menuService.findOne(id));
      menuSet = menuService.getChildMenus(menuMapper.toEntity(menuList), menuSet);
    }
    menuService.delete(menuSet);
    return new ResponseEntity<>(HttpStatus.OK);
  }
}
