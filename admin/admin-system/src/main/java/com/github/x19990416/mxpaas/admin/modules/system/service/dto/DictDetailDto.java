/** create by Guo Limin on 2021/1/30. */
package com.github.x19990416.mxpaas.admin.modules.system.service.dto;

import com.github.x19990416.mxpaas.admin.common.base.BaseDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = true)
public class DictDetailDto extends BaseDTO implements Serializable {

  private Long id;

  private DictSmallDto dict;

  private String label;

  private String value;

  private Integer dictSort;
}
