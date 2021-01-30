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
package com.github.x19990416.mxpaas.admin.modules.system.repository;

import com.github.x19990416.mxpaas.admin.modules.system.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Date;
import java.util.List;
import java.util.Set;

public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {
  @Query(
      value =
          "SELECT u.* FROM sys_user u, sys_users_roles r WHERE"
              + " u.user_id = r.user_id AND r.role_id = ?1",
      nativeQuery = true)
  public List<User> findByRoleId(Long roleId);

  @Query(
      value =
          "SELECT count(1) FROM sys_user u, sys_users_roles r WHERE "
              + "u.user_id = r.user_id AND r.role_id in ?1",
      nativeQuery = true)
  public int countByRoles(Set<Long> ids);

  public User findByUsername(String username);

  public User findByEmail(String email);

  public User findByPhone(String phone);

  public void deleteAllByIdIn(Set<Long> ids);

  @Modifying
  @Query(
      value = "update sys_user set password = ?2 , pwd_reset_time = ?3 where username = ?1",
      nativeQuery = true)
  void updatePass(String username, String pass, Date lastPasswordResetTime);

  @Modifying
  @Query(value = "update sys_user set email = ?2 where username = ?1", nativeQuery = true)
  void updateEmail(String username, String email);

  @Query(value = "SELECT count(1) FROM sys_user u WHERE u.dept_id IN ?1", nativeQuery = true)
  int countByDepts(Set<Long> deptIds);

  @Query(
      value =
          "SELECT u.* FROM sys_user u, sys_users_roles r, sys_roles_depts d WHERE "
              + "u.user_id = r.user_id AND r.role_id = d.role_id AND r.role_id = ?1 group by u.user_id",
      nativeQuery = true)
  List<User> findByDeptRoleId(Long id);

  @Query(
      value =
          "SELECT count(1) FROM sys_user u, sys_users_jobs j WHERE u.user_id = j.user_id AND j.job_id IN ?1",
      nativeQuery = true)
  int countByJobs(Set<Long> ids);

  @Query(
      value =
          "SELECT u.* FROM sys_user u, sys_users_roles ur, sys_roles_menus rm WHERE\n"
              + "u.user_id = ur.user_id AND ur.role_id = rm.role_id AND rm.menu_id = ?1 group by u.user_id",
      nativeQuery = true)
  List<User> findByMenuId(Long id);
}
