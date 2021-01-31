/** create by Guo Limin on 2021/1/31. */
package com.github.x19990416.mxpaas.admin.modules.system.domain.vo;

import lombok.Data;

@Data
public class UserPwdVo {
  private String oldPass;

  private String newPass;
}
