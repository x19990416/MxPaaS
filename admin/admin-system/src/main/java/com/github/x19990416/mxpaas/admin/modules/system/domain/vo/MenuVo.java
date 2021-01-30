/** create by Guo Limin on 2021/1/30. */
package com.github.x19990416.mxpaas.admin.modules.system.domain.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@Accessors
public class MenuVo implements Serializable {

  private String name;

  private String path;

  private Boolean hidden;

  private String redirect;

  private String component;

  private Boolean alwaysShow;

  private MenuMetaVo meta;

  private List<MenuVo> children;
}
