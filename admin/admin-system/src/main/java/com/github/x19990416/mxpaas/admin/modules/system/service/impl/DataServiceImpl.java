/** create by Guo Limin on 2021/1/29. */
package com.github.x19990416.mxpaas.admin.modules.system.service.impl;

import com.github.x19990416.mxpaas.admin.common.utils.enums.DataScopeEnum;
import com.github.x19990416.mxpaas.admin.modules.system.domain.Dept;
import com.github.x19990416.mxpaas.admin.modules.system.service.DataService;
import com.github.x19990416.mxpaas.admin.modules.system.service.DeptService;
import com.github.x19990416.mxpaas.admin.modules.system.service.RoleService;
import com.github.x19990416.mxpaas.admin.modules.system.service.dto.RoleSmallDto;
import com.github.x19990416.mxpaas.admin.modules.system.service.dto.UserDto;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
@CacheConfig(cacheNames = "data")
public class DataServiceImpl implements DataService {
  RoleService roleService;
  DeptService deptService;

  @Override
  @Cacheable(key = "'user:' + #p0.id")
  public List<Long> getDeptIds(UserDto user) {
    // 用于存储部门id
    Set<Long> deptIds = new HashSet<>();
    // 查询用户角色
    List<RoleSmallDto> roleSet = roleService.findByUsersId(user.getId());
    // 获取对应的部门ID
    for (RoleSmallDto role : roleSet) {
      DataScopeEnum dataScopeEnum = DataScopeEnum.find(role.getDataScope());
      switch (Objects.requireNonNull(dataScopeEnum)) {
        case THIS_LEVEL:
          deptIds.add(user.getDept().getId());
          break;
        case CUSTOMIZE:
          deptIds.addAll(getCustomize(deptIds, role));
          break;
        default:
          return new ArrayList<>(deptIds);
      }
    }
    return new ArrayList<>(deptIds);
  }

  /**
   * 获取自定义的数据权限
   *
   * @param deptIds 部门ID
   * @param role 角色
   * @return 数据权限ID
   */
  public Set<Long> getCustomize(Set<Long> deptIds, RoleSmallDto role) {
    Set<Dept> depts = deptService.findByRoleId(role.getId());
    for (Dept dept : depts) {
      deptIds.add(dept.getId());
      List<Dept> deptChildren = deptService.findByPid(dept.getId());
      if (deptChildren != null && deptChildren.size() != 0) {
        deptIds.addAll(deptService.getDeptChildren(deptChildren));
      }
    }
    return deptIds;
  }
}
