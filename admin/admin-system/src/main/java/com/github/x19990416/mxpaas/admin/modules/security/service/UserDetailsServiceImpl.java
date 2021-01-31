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
package com.github.x19990416.mxpaas.admin.modules.security.service;

import com.github.x19990416.mxpaas.admin.common.exception.BadRequestException;
import com.github.x19990416.mxpaas.admin.modules.security.service.dto.JwtUserDto;
import com.github.x19990416.mxpaas.admin.modules.system.service.dto.UserDto;
import com.github.x19990416.mxpaas.admin.modules.security.config.bean.LoginProperties;
import com.github.x19990416.mxpaas.admin.modules.system.service.DataService;
import com.github.x19990416.mxpaas.admin.modules.system.service.RoleService;
import com.github.x19990416.mxpaas.admin.modules.system.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
@RequiredArgsConstructor
@Service
public class UserDetailsServiceImpl implements UserDetailsService {
	private final UserService userService;
	private final RoleService roleService;
	private final DataService dataService;
	private final LoginProperties loginProperties;
	public void setEnableCache(boolean enableCache) {
		this.loginProperties.setCacheEnable(enableCache);
	}

	/**
	 * 用户信息缓存
	 *
	 * @see {@link UserCacheClean}
	 */
	static Map<String, JwtUserDto> userDtoCache = new ConcurrentHashMap<>();

	@Override
	public JwtUserDto loadUserByUsername(String username) {
		boolean searchDb = true;
		JwtUserDto jwtUserDto = null;
		if (loginProperties.isCacheEnable() && userDtoCache.containsKey(username)) {
			jwtUserDto = userDtoCache.get(username);
			searchDb = false;
		}
		if (searchDb) {
			UserDto user;
			try {
				user = userService.findByName(username);
			} catch (EntityNotFoundException e) {
				// SpringSecurity会自动转换UsernameNotFoundException为BadCredentialsException
				throw new UsernameNotFoundException("", e);
			}
			if (user == null) {
				throw new UsernameNotFoundException("");
			} else {
				if (!user.getEnabled()) {
					throw new BadRequestException("账号未激活！");
				}
				jwtUserDto = new JwtUserDto(
						user,
						dataService.getDeptIds(user),
						roleService.mapToGrantedAuthorities(user)
				);
				userDtoCache.put(username, jwtUserDto);
			}
		}
		return jwtUserDto;
	}
}
