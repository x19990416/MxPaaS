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
package com.github.x19990416.mxpaas.admin.modules.security.security;

import com.github.x19990416.mxpaas.admin.common.utils.KeyUtil;
import com.github.x19990416.mxpaas.admin.common.utils.RedisUtil;
import com.github.x19990416.mxpaas.admin.modules.security.config.bean.SecurityProperties;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.security.Key;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class TokenProvider implements InitializingBean {
  private final SecurityProperties properties;
  private final RedisUtil redisUtil;
  public static final String AUTHORITIES_KEY = "user";
  private JwtParser jwtParser;
  private JwtBuilder jwtBuilder;

  @Override
  public void afterPropertiesSet() throws Exception {
    byte[] keyBytes = Decoders.BASE64.decode(properties.getBase64Secret());
    Key key = Keys.hmacShaKeyFor(keyBytes);
    jwtParser = Jwts.parserBuilder().setSigningKey(key).build();
    jwtBuilder = Jwts.builder().signWith(key, SignatureAlgorithm.HS512);
  }

  public String createToken(Authentication authentication) {
    return jwtBuilder
        // 加入ID确保生成的 Token 都不一致
        .setId(KeyUtil.randomUUID())
        .claim(AUTHORITIES_KEY, authentication.getName())
        .setSubject(authentication.getName())
        .compact();
  }

  Authentication getAuthentication(String token) {
    Claims claims = getClaims(token);
    User principal = new User(claims.getSubject(), "******", new ArrayList<>());
    return new UsernamePasswordAuthenticationToken(principal, token, new ArrayList<>());
  }

  public Claims getClaims(String token) {
    return jwtParser.parseClaimsJws(token).getBody();
  }

  public void checkRenewal(String token) {
    // 判断是否续期token,计算token的过期时间
    long time = redisUtil.getExpire(properties.getOnlineKey() + token) * 1000;
    Date expireDate = new Date(System.currentTimeMillis() + time);
    // 判断当前时间与过期时间的时间差
    long differ = expireDate.getTime() - System.currentTimeMillis();
    // 如果在续期检查的范围内，则续期
    if (differ <= properties.getDetect()) {
      long renew = time + properties.getRenew();
      redisUtil.expire(properties.getOnlineKey() + token, renew, TimeUnit.MILLISECONDS);
    }
  }

  public String getToken(HttpServletRequest request) {
    final String requestHeader = request.getHeader(properties.getHeader());
    if (requestHeader != null && requestHeader.startsWith(properties.getTokenStartWith())) {
      return requestHeader.substring(7);
    }
    return null;
  }
}
