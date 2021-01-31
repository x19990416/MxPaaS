/** create by Guo Limin on 2021/1/31. */
package com.github.x19990416.mxpaas.admin.modules.security.rest;

import com.github.x19990416.mxpaas.admin.common.annotation.controller.AnonymousDeleteMapping;
import com.github.x19990416.mxpaas.admin.common.annotation.controller.AnonymousGetMapping;
import com.github.x19990416.mxpaas.admin.common.annotation.controller.AnonymousPostMapping;
import com.github.x19990416.mxpaas.admin.common.config.RsaProperties;
import com.github.x19990416.mxpaas.admin.common.exception.BadRequestException;
import com.github.x19990416.mxpaas.admin.common.utils.KeyUtil;
import com.github.x19990416.mxpaas.admin.common.utils.RedisUtil;
import com.github.x19990416.mxpaas.admin.common.utils.RsaUtil;
import com.github.x19990416.mxpaas.admin.common.utils.SecurityUtils;
import com.github.x19990416.mxpaas.admin.modules.security.config.bean.LoginCodeEnum;
import com.github.x19990416.mxpaas.admin.modules.security.config.bean.LoginProperties;
import com.github.x19990416.mxpaas.admin.modules.security.config.bean.SecurityProperties;
import com.github.x19990416.mxpaas.admin.modules.security.security.TokenProvider;
import com.github.x19990416.mxpaas.admin.modules.security.service.OnlineUserService;
import com.github.x19990416.mxpaas.admin.modules.security.service.dto.AuthUserDto;
import com.github.x19990416.mxpaas.admin.modules.security.service.dto.JwtUserDto;
import com.wf.captcha.base.Captcha;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.security.Key;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "系统：系统授权接口")
public class AuthorizationController {
  private final SecurityProperties properties;
  private final RedisUtil redisUtils;
  private final OnlineUserService onlineUserService;
  private final TokenProvider tokenProvider;
  private final AuthenticationManagerBuilder authenticationManagerBuilder;
  @Resource private LoginProperties loginProperties;

  @Operation(method = "登录授权")
  @AnonymousPostMapping(value = "/login")
  public ResponseEntity<Object> login(
      @Validated @RequestBody AuthUserDto authUser, HttpServletRequest request) throws Exception {
    // 密码解密
    String password = RsaUtil.decryptByPrivateKey(RsaProperties.privateKey, authUser.getPassword());
    // 查询验证码
    String code = (String) redisUtils.get(authUser.getUuid());
    // 清除验证码
    redisUtils.del(authUser.getUuid());
    if (StringUtils.isBlank(code)) {
      throw new BadRequestException("验证码不存在或已过期");
    }
    if (StringUtils.isBlank(authUser.getCode()) || !authUser.getCode().equalsIgnoreCase(code)) {
      throw new BadRequestException("验证码错误");
    }
    UsernamePasswordAuthenticationToken authenticationToken =
        new UsernamePasswordAuthenticationToken(authUser.getUsername(), password);
    Authentication authentication =
        authenticationManagerBuilder.getObject().authenticate(authenticationToken);
    SecurityContextHolder.getContext().setAuthentication(authentication);
    // 生成令牌
    String token = tokenProvider.createToken(authentication);
    final JwtUserDto jwtUserDto = (JwtUserDto) authentication.getPrincipal();
    // 保存在线信息
    onlineUserService.save(jwtUserDto, token, request);
    // 返回 token 与 用户信息
    Map<String, Object> authInfo =
        new HashMap<String, Object>(2) {
          {
            put("token", properties.getTokenStartWith() + token);
            put("user", jwtUserDto);
          }
        };
    if (loginProperties.isSingleLogin()) {
      // 踢掉之前已经登录的token
      onlineUserService.checkLoginOnUser(authUser.getUsername(), token);
    }
    return ResponseEntity.ok(authInfo);
  }

  @Operation(method = "获取用户信息")
  @GetMapping(value = "/info")
  public ResponseEntity<Object> getUserInfo() {
    return ResponseEntity.ok(SecurityUtils.getCurrentUser());
  }

  @Operation(method = "获取验证码")
  @AnonymousGetMapping(value = "/code")
  public ResponseEntity<Object> getCode() {
    // 获取运算的结果
    Captcha captcha = loginProperties.getCaptcha();
    String uuid = properties.getCodeKey() + KeyUtil.randomUUID();
    // 当验证码类型为 arithmetic时且长度 >= 2 时，captcha.text()的结果有几率为浮点型
    String captchaValue = captcha.text();
    if (captcha.getCharType() - 1 == LoginCodeEnum.arithmetic.ordinal()
        && captchaValue.contains(".")) {
      captchaValue = captchaValue.split("\\.")[0];
    }
    // 保存
    redisUtils.set(
        uuid, captchaValue, loginProperties.getLoginCode().getExpiration(), TimeUnit.MINUTES);
    // 验证码信息
    Map<String, Object> imgResult =
        new HashMap<String, Object>(2) {
          {
            put("img", captcha.toBase64());
            put("uuid", uuid);
          }
        };
    return ResponseEntity.ok(imgResult);
  }

  @Operation(method = "退出登录")
  @AnonymousDeleteMapping(value = "/logout")
  public ResponseEntity<Object> logout(HttpServletRequest request) {
    onlineUserService.logout(tokenProvider.getToken(request));
    return new ResponseEntity<>(HttpStatus.OK);
  }
}
