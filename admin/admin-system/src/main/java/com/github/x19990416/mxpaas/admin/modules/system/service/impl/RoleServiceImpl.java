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
package com.github.x19990416.mxpaas.admin.modules.system.service.impl;

import com.github.x19990416.mxpaas.admin.modules.system.domain.Role;
import com.github.x19990416.mxpaas.admin.modules.system.service.RoleService;
import com.github.x19990416.mxpaas.admin.modules.system.service.dto.RoleDto;
import com.github.x19990416.mxpaas.admin.modules.system.service.dto.RoleQueryCriteria;
import com.github.x19990416.mxpaas.admin.modules.system.service.dto.RoleSmallDto;
import com.github.x19990416.mxpaas.admin.modules.system.service.dto.UserDto;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Set;

@Service
public class RoleServiceImpl implements RoleService {
	@Override
	public List<RoleDto> queryAll() {
		return null;
	}

	@Override
	public RoleDto findById(long id) {
		return null;
	}

	@Override
	public void create(Role resources) {

	}

	@Override
	public void update(Role resources) {

	}

	@Override
	public void delete(Set<Long> ids) {

	}

	@Override
	public List<RoleSmallDto> findByUsersId(Long id) {
		return null;
	}

	@Override
	public Integer findByRoles(Set<Role> roles) {
		return null;
	}

	@Override
	public void updateMenu(Role resources, RoleDto roleDTO) {

	}

	@Override
	public void untiedMenu(Long id) {

	}

	@Override
	public Object queryAll(RoleQueryCriteria criteria, Pageable pageable) {
		return null;
	}

	@Override
	public List<RoleDto> queryAll(RoleQueryCriteria criteria) {
		return null;
	}

	@Override
	public void download(List<RoleDto> queryAll, HttpServletResponse response) throws IOException {

	}

	@Override
	public List<GrantedAuthority> mapToGrantedAuthorities(UserDto user) {
		return null;
	}

	@Override
	public void verification(Set<Long> ids) {

	}

	@Override
	public List<Role> findInMenuId(List<Long> menuIds) {
		return null;
	}
}
