/** create by Guo Limin on 2021/1/30. */
package com.github.x19990416.mxpaas.admin.common.utils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.x19990416.mxpaas.admin.common.exception.BadRequestException;
import com.github.x19990416.mxpaas.admin.common.utils.enums.DataScopeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;

@Slf4j
public class SecurityUtil {

  /**
   * 获取当前登录的用户
   *
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
   * 获取系统用户ID
   *
   * @return 系统用户ID
   */
  public static Long getCurrentUserId() {
    UserDetails userDetails = getCurrentUser();
    JSONObject jsonObject = (JSONObject) JSONObject.toJSON(userDetails);
    JSONObject userJsonObject = (JSONObject) JSONObject.toJSON(jsonObject.get("user"));

    return userJsonObject.getLong("id");
  }

  /**
   * 获取当前用户的数据权限
   *
   * @return /
   */
  public static List<Long> getCurrentUserDataScope() {
    UserDetails userDetails = getCurrentUser();
    Object dataScopes = ((JSONObject) JSONObject.toJSON(userDetails)).get("dataScopes");
    return JSONArray.parseArray(JSONObject.toJSONString(dataScopes), Long.class);
  }

  /**
   * 获取数据权限级别
   *
   * @return 级别
   */
  public static String getDataScopeType() {
    List<Long> dataScopes = getCurrentUserDataScope();
    if (dataScopes.size() != 0) {
      return "";
    }
    return DataScopeEnum.ALL.getValue();
  }
}
