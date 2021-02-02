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
package com.github.x19990416.mxpaas.modules.secuity.filter;

import com.github.x19990416.mxpaas.admin.common.exception.BadRequestException;
import com.github.x19990416.mxpaas.modules.secuity.SecurityConstants;
import com.github.x19990416.mxpaas.modules.secuity.token.SmsCodeAuthenticationToken;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class SmsCodeAuthenticationFilter extends AbstractAuthenticationProcessingFilter {
  // 请求接口的url
  public SmsCodeAuthenticationFilter() {
    super(new AntPathRequestMatcher(SecurityConstants.DEFAULT_LOGIN_PROCESSING_URL_MOBILE, "POST"));
  }

  @Override
  public Authentication attemptAuthentication(
      HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
    if (!"POST".equals(request.getMethod())) {
      throw new AuthenticationServiceException(
          "Authentication method not supported: " + request.getMethod());
    }
    // 根据请求参数名，获取请求value
    String mobile = request.getParameter(SecurityConstants.DEFAULT_PARAMETER_NAME_MOBILE);
    if (StringUtils.isEmpty(mobile)) throw new BadRequestException("参数不正确");

    mobile = mobile.trim();
    // 生成对应的AuthenticationToken
    SmsCodeAuthenticationToken authRequest = new SmsCodeAuthenticationToken(mobile);

    setDetails(request, authRequest);

    return this.getAuthenticationManager().authenticate(authRequest);
  }

  protected void setDetails(HttpServletRequest request, SmsCodeAuthenticationToken authRequest) {
    authRequest.setDetails(authenticationDetailsSource.buildDetails(request));
  }
}
