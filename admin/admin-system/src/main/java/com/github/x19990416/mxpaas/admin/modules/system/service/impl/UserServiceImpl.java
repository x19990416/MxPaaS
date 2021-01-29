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

import com.github.x19990416.mxpaas.admin.common.config.FileProperties;
import com.github.x19990416.mxpaas.admin.common.utils.RedisUtil;
import com.github.x19990416.mxpaas.admin.modules.security.service.OnlineUserService;
import com.github.x19990416.mxpaas.admin.modules.security.service.UserCacheClean;
import com.github.x19990416.mxpaas.admin.modules.system.domain.User;
import com.github.x19990416.mxpaas.admin.modules.system.repository.UserRepository;
import com.github.x19990416.mxpaas.admin.modules.system.service.UserService;
import com.github.x19990416.mxpaas.admin.modules.system.service.dto.UserDto;
import com.github.x19990416.mxpaas.admin.modules.system.service.dto.UserMapper;
import com.github.x19990416.mxpaas.admin.modules.system.service.dto.UserQueryCriteria;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
@CacheConfig(cacheNames = "user")
public class UserServiceImpl implements UserService {
	UserRepository userRepository;
	private final UserMapper userMapper;
	private final FileProperties properties;
	private final RedisUtil redisUtils;
	private final UserCacheClean userCacheClean;
	private final OnlineUserService onlineUserService;
	@Override
	public UserDto findById(long id) {
		return null;
	}

	@Override
	public void create(User resources) {

	}

	@Override
	public void update(User resources) throws Exception {

	}

	@Override
	public void delete(Set<Long> ids) {

	}

	@Override
	public UserDto findByName(String userName) {
		return null;
	}

	@Override
	public void updatePass(String username, String encryptPassword) {

	}

	@Override
	public Map<String, String> updateAvatar(MultipartFile file) {
		return null;
	}

	@Override
	public void updateEmail(String username, String email) {

	}

	@Override
	public Object queryAll(UserQueryCriteria criteria, Pageable pageable) {
		return null;
	}

	@Override
	public List<UserDto> queryAll(UserQueryCriteria criteria) {
		return null;
	}

	@Override
	public void download(List<UserDto> queryAll, HttpServletResponse response) throws IOException {

	}

	@Override
	public void updateCenter(User resources) {

	}
}
