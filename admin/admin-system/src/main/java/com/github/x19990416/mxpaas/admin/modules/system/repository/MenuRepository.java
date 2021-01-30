/** create by Guo Limin on 2021/1/30. */
package com.github.x19990416.mxpaas.admin.modules.system.repository;

import com.github.x19990416.mxpaas.admin.modules.system.domain.Menu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public interface MenuRepository extends JpaRepository<Menu, Long>, JpaSpecificationExecutor<Menu> {

  Menu findByTitle(String title);

  Menu findByComponentName(String name);

  List<Menu> findByPid(long pid);

  List<Menu> findByPidIsNull();

  @Query(
      value =
          "SELECT m.* FROM sys_menu m, sys_roles_menus r WHERE "
              + "m.menu_id = r.menu_id AND r.role_id IN ?1 AND type != ?2 order by m.menu_sort asc",
      nativeQuery = true)
  LinkedHashSet<Menu> findByRoleIdsAndTypeNot(Set<Long> roleIds, int type);

  int countByPid(Long id);

  @Modifying
  @Query(value = " update sys_menu set sub_count = ?1 where menu_id = ?2 ", nativeQuery = true)
  void updateSubCntById(int count, Long menuId);
}
