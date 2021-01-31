/**
 * create by Guo Limin on 2021/1/31.
 */
package com.github.x19990416.mxpaas.admin.modules.system.service.impl;

import com.github.x19990416.mxpaas.admin.common.exception.BadRequestException;
import com.github.x19990416.mxpaas.admin.common.utils.RedisUtil;
import com.github.x19990416.mxpaas.admin.modules.system.domain.vo.EmailVo;
import com.github.x19990416.mxpaas.admin.modules.system.service.VerifyService;
import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ResourceUtils;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class VerifyServiceImpl implements VerifyService {

  @Value("${code.expiration}")
  private Long expiration;

  private final RedisUtil redisUtil;
  ResourceUtils r;

  @Override
  @Transactional(rollbackFor = Exception.class)
  public EmailVo sendEmail(String email, String key) {
    /* EmailVo emailVo;
    String content;
    String redisKey = key + email;
    // 如果不存在有效的验证码，就创建一个新的
    TemplateEngine engine = TemplateUtil.createEngine(new TemplateConfig("template", TemplateConfig.ResourceMode.CLASSPATH));
    Template template = engine.getTemplate("email/email.ftl");
    Object oldCode =  redisUtil.get(redisKey);
    if(oldCode == null){
        String code = RandomUtil.randomNumbers (6);
        // 存入缓存
        if(!redisUtils.set(redisKey, code, expiration)){
            throw new BadRequestException("服务异常，请联系网站负责人");
        }
        content = template.render(Dict.create().set("code",code));
        emailVo = new EmailVo(Collections.singletonList(email),"EL-ADMIN后台管理系统",content);
        // 存在就再次发送原来的验证码
    } else {
        content = template.render(Dict.create().set("code",oldCode));
        emailVo = new EmailVo(Collections.singletonList(email),"EL-ADMIN后台管理系统",content);
    }*/
    EmailVo emailVo =
        new EmailVo().setContent("未实现").setSubject("未实现").setTos(Lists.newArrayList());
    return emailVo;
  }

  @Override
  public void validated(String key, String code) {
    Object value = redisUtil.get(key);
    if (value == null || !value.toString().equals(code)) {
      throw new BadRequestException("无效验证码");
    } else {
      redisUtil.del(key);
    }
  }
}