/** create by Guo Limin on 2021/1/30. */
package com.github.x19990416.mxpaas.admin.modules.system.service;

import com.github.x19990416.mxpaas.admin.modules.system.domain.Menu;
import com.github.x19990416.mxpaas.admin.modules.system.domain.vo.MenuVo;
import com.github.x19990416.mxpaas.admin.modules.system.service.dto.MenuDto;
import com.github.x19990416.mxpaas.admin.modules.system.service.dto.MenuQueryCriteria;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Set;

public interface MenuService {

  List<MenuDto> queryAll(MenuQueryCriteria criteria, Boolean isQuery) throws Exception;

  MenuDto findById(long id);

  void create(Menu resources);

  void update(Menu resources);

  Set<Menu> getChildMenus(List<Menu> menuList, Set<Menu> menuSet);

  List<MenuDto> buildTree(List<MenuDto> menuDtos);

  Object buildMenus(List<MenuDto> menuDtos);

  Menu findOne(Long id);

  void delete(Set<Menu> menuSet);

  void download(List<MenuDto> queryAll, HttpServletResponse response) throws IOException;

  List<MenuDto> getMenus(Long pid);

  List<MenuDto> getSuperior(MenuDto menuDto, List<Menu> objects);

  List<MenuDto> findByUser(Long currentUserId);
}
