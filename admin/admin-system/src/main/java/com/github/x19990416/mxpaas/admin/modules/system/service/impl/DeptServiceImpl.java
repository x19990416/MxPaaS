/** create by Guo Limin on 2021/1/30. */
package com.github.x19990416.mxpaas.admin.modules.system.service.impl;

import com.github.x19990416.mxpaas.admin.common.exception.BadRequestException;
import com.github.x19990416.mxpaas.admin.common.utils.*;
import com.github.x19990416.mxpaas.admin.common.utils.enums.DataScopeEnum;
import com.github.x19990416.mxpaas.admin.modules.system.domain.Dept;
import com.github.x19990416.mxpaas.admin.modules.system.domain.User;
import com.github.x19990416.mxpaas.admin.modules.system.repository.DeptRepository;
import com.github.x19990416.mxpaas.admin.modules.system.repository.RoleRepository;
import com.github.x19990416.mxpaas.admin.modules.system.repository.UserRepository;
import com.github.x19990416.mxpaas.admin.modules.system.service.DeptService;
import com.github.x19990416.mxpaas.admin.modules.system.service.dto.DeptDto;
import com.github.x19990416.mxpaas.admin.modules.system.service.dto.DeptMapper;
import com.github.x19990416.mxpaas.admin.modules.system.service.dto.DeptQueryCriteria;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@CacheConfig(cacheNames = "dept")
public class DeptServiceImpl implements DeptService {

  private final DeptRepository deptRepository;
  private final DeptMapper deptMapper;
  private final UserRepository userRepository;
  private final RedisUtil redisUtils;
  private final RoleRepository roleRepository;

  @Override
  public List<DeptDto> queryAll(DeptQueryCriteria criteria, Boolean isQuery) throws Exception {
    Sort sort = Sort.by(Sort.Direction.ASC, "deptSort");
    String dataScopeType = SecurityUtil.getDataScopeType();
    if (isQuery) {
      if (dataScopeType.equals(DataScopeEnum.ALL.getValue())) {
        criteria.setPidIsNull(true);
      }
      List<Field> fields = QueryHelper.getAllFields(criteria.getClass(), new ArrayList<>());
      List<String> fieldNames =
          new ArrayList<String>() {
            {
              add("pidIsNull");
              add("enabled");
            }
          };
      for (Field field : fields) {
        // 设置对象的访问权限，保证对private的属性的访问
        field.setAccessible(true);
        Object val = field.get(criteria);
        if (fieldNames.contains(field.getName())) {
          continue;
        }
        if (null == val) {
          criteria.setPidIsNull(null);
          break;
        }
      }
    }
    List<DeptDto> list =
        deptMapper.toDto(
            deptRepository.findAll(
                (root, criteriaQuery, criteriaBuilder) ->
                    QueryHelper.getPredicate(root, criteria, criteriaBuilder),
                sort));
    // 如果为空，就代表为自定义权限或者本级权限，就需要去重，不理解可以注释掉，看查询结果
    if (Strings.isBlank(dataScopeType)) {
      return deduplication(list);
    }
    return list;
  }

  @Override
  @Cacheable(key = "'id:' + #p0")
  public DeptDto findById(Long id) {
    Dept dept = deptRepository.findById(id).orElseGet(Dept::new);
    ValidationUtil.isNull(dept.getId(), "Dept", "id", id);
    return deptMapper.toDto(dept);
  }

  @Override
  public List<Dept> findByPid(long pid) {
    return deptRepository.findByPid(pid);
  }

  @Override
  public Set<Dept> findByRoleId(Long id) {
    return deptRepository.findByRoleId(id);
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public void create(Dept resources) {
    deptRepository.save(resources);
    // 计算子节点数目
    resources.setSubCount(0);
    // 清理缓存
    updateSubCnt(resources.getPid());
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public void update(Dept resources) {
    // 旧的部门
    Long oldPid = findById(resources.getId()).getPid();
    Long newPid = resources.getPid();
    if (resources.getPid() != null && resources.getId().equals(resources.getPid())) {
      throw new BadRequestException("上级不能为自己");
    }
    Dept dept = deptRepository.findById(resources.getId()).orElseGet(Dept::new);
    ValidationUtil.isNull(dept.getId(), "Dept", "id", resources.getId());
    resources.setId(dept.getId());
    deptRepository.save(resources);
    // 更新父节点中子节点数目
    updateSubCnt(oldPid);
    updateSubCnt(newPid);
    // 清理缓存
    delCaches(resources.getId());
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public void delete(Set<DeptDto> deptDtos) {
    for (DeptDto deptDto : deptDtos) {
      // 清理缓存
      delCaches(deptDto.getId());
      deptRepository.deleteById(deptDto.getId());
      updateSubCnt(deptDto.getPid());
    }
  }

  @Override
  public void download(List<DeptDto> deptDtos, HttpServletResponse response) throws IOException {
    List<Map<String, Object>> list = new ArrayList<>();
    for (DeptDto deptDTO : deptDtos) {
      Map<String, Object> map = new LinkedHashMap<>();
      map.put("部门名称", deptDTO.getName());
      map.put("部门状态", deptDTO.getEnabled() ? "启用" : "停用");
      map.put("创建日期", deptDTO.getCreateTime());
      list.add(map);
    }
    FileUtil.downloadExcel(list, response);
  }

  @Override
  public Set<DeptDto> getDeleteDepts(List<Dept> menuList, Set<DeptDto> deptDtos) {
    for (Dept dept : menuList) {
      deptDtos.add(deptMapper.toDto(dept));
      List<Dept> depts = deptRepository.findByPid(dept.getId());
      if (depts != null && depts.size() != 0) {
        getDeleteDepts(depts, deptDtos);
      }
    }
    return deptDtos;
  }

  @Override
  public List<Long> getDeptChildren(List<Dept> deptList) {
    List<Long> list = new ArrayList<>();
    deptList.forEach(
        dept -> {
          if (dept != null && dept.getEnabled()) {
            List<Dept> depts = deptRepository.findByPid(dept.getId());
            if (deptList.size() != 0) {
              list.addAll(getDeptChildren(depts));
            }
            list.add(dept.getId());
          }
        });
    return list;
  }

  @Override
  public List<DeptDto> getSuperior(DeptDto deptDto, List<Dept> depts) {
    if (deptDto.getPid() == null) {
      depts.addAll(deptRepository.findByPidIsNull());
      return deptMapper.toDto(depts);
    }
    depts.addAll(deptRepository.findByPid(deptDto.getPid()));
    return getSuperior(findById(deptDto.getPid()), depts);
  }

  @Override
  public Object buildTree(List<DeptDto> deptDtos) {
    Set<DeptDto> trees = new LinkedHashSet<>();
    Set<DeptDto> depts = new LinkedHashSet<>();
    List<String> deptNames = deptDtos.stream().map(DeptDto::getName).collect(Collectors.toList());
    boolean isChild;
    for (DeptDto deptDTO : deptDtos) {
      isChild = false;
      if (deptDTO.getPid() == null) {
        trees.add(deptDTO);
      }
      for (DeptDto it : deptDtos) {
        if (it.getPid() != null && deptDTO.getId().equals(it.getPid())) {
          isChild = true;
          if (deptDTO.getChildren() == null) {
            deptDTO.setChildren(new ArrayList<>());
          }
          deptDTO.getChildren().add(it);
        }
      }
      if (isChild) {
        depts.add(deptDTO);
      } else if (deptDTO.getPid() != null
          && !deptNames.contains(findById(deptDTO.getPid()).getName())) {
        depts.add(deptDTO);
      }
    }

    if (CollectionUtils.isEmpty(trees)) {
      trees = depts;
    }
    Map<String, Object> map = new HashMap<>(2);
    map.put("totalElements", deptDtos.size());
    map.put("content", CollectionUtils.isEmpty(trees) ? deptDtos : trees);
    return map;
  }

  @Override
  public void verification(Set<DeptDto> deptDtos) {
    Set<Long> deptIds = deptDtos.stream().map(DeptDto::getId).collect(Collectors.toSet());
    if (userRepository.countByDepts(deptIds) > 0) {
      throw new BadRequestException("所选部门存在用户关联，请解除后再试！");
    }
    if (roleRepository.countByDepts(deptIds) > 0) {
      throw new BadRequestException("所选部门存在角色关联，请解除后再试！");
    }
  }

  private void updateSubCnt(Long deptId) {
    if (deptId != null) {
      int count = deptRepository.countByPid(deptId);
      deptRepository.updateSubCntById(count, deptId);
    }
  }

  private List<DeptDto> deduplication(List<DeptDto> list) {
    List<DeptDto> deptDtos = new ArrayList<>();
    for (DeptDto deptDto : list) {
      boolean flag = true;
      for (DeptDto dto : list) {
        if (dto.getId().equals(deptDto.getPid())) {
          flag = false;
          break;
        }
      }
      if (flag) {
        deptDtos.add(deptDto);
      }
    }
    return deptDtos;
  }

  /**
   * 清理缓存
   *
   * @param id /
   */
  public void delCaches(Long id) {
    List<User> users = userRepository.findByDeptRoleId(id);
    // 删除数据权限
    redisUtils.delByKeys(
        CacheKey.DATA_USER, users.stream().map(User::getId).collect(Collectors.toSet()));
    redisUtils.del(CacheKey.DEPT_ID + id);
  }
}
