/** create by Guo Limin on 2021/1/30. */
package com.github.x19990416.mxpaas.admin.common.utils;

import com.github.x19990416.mxpaas.admin.common.exception.BadRequestException;
import org.apache.commons.validator.routines.EmailValidator;

public class ValidationUtil {

  /** 验证空 */
  public static void isNull(Object obj, String entity, String parameter, Object value) {
    if (obj == null) {
      String msg = entity + " 不存在: " + parameter + " is " + value;
      throw new BadRequestException(msg);
    }
  }

  /** 验证是否为邮箱 */
  public static boolean isEmail(String email) {
    return EmailValidator.getInstance().isValid(email);
  }
}
