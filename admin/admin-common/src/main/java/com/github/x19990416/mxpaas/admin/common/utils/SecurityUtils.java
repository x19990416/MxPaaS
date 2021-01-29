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
package com.github.x19990416.mxpaas.admin.common.utils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.x19990416.mxpaas.admin.common.exception.BadRequestException;
import com.github.x19990416.mxpaas.admin.common.utils.enums.DataScopeEnum;
import org.springframework.boot.jackson.JsonObjectDeserializer;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;

public class SecurityUtils {
	/**
	 * 获取当前登录的用户
	 * @return UserDetails
	 */
	public static UserDetails getCurrentUser() {
		UserDetailsService userDetailsService = SpringContextHolder.getBean(UserDetailsService.class);
		return userDetailsService.loadUserByUsername(getCurrentUsername());
	}
	/**
	 * 获取系统用户名称
	 *
	 * @return 系统用户名称
	 */
	public static String getCurrentUsername() {
		final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null) {
			throw new BadRequestException(HttpStatus.UNAUTHORIZED, "当前登录状态过期");
		}
		if (authentication.getPrincipal() instanceof UserDetails) {
			UserDetails userDetails = (UserDetails) authentication.getPrincipal();
			return userDetails.getUsername();
		}
		throw new BadRequestException(HttpStatus.UNAUTHORIZED, "找不到当前登录的信息");
	}

	/**
	 * 获取当前用户的数据权限
	 * @return /
	 */
	public static List<Long> getCurrentUserDataScope(){
		UserDetails userDetails = getCurrentUser();
		JSONObject jsonObj = (JSONObject)JSONObject.toJSON(userDetails);
    	return  JSONArray.parseArray(jsonObj.get("dataScopes").toString(),Long.class);
	}

	/**
	 * 获取数据权限级别
	 * @return 级别
	 */
	public static String getDataScopeType() {
		List<Long> dataScopes = getCurrentUserDataScope();
		if(dataScopes.size() != 0){
			return "";
		}
		return DataScopeEnum.ALL.getValue();
	}

}
