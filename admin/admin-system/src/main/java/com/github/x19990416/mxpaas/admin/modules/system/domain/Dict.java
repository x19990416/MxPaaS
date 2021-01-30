/** create by Guo Limin on 2021/1/30. */
package com.github.x19990416.mxpaas.admin.modules.system.domain;

import com.github.x19990416.mxpaas.admin.common.base.BaseEntity;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

@Entity
@Data
@EqualsAndHashCode(callSuper = true)
@Table(name = "sys_dict")
public class Dict extends BaseEntity implements Serializable {

  @Id
  @Column(name = "dict_id")
  @NotNull(groups = Update.class)
  @Schema(name = "ID", hidden = true)
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @OneToMany(
      mappedBy = "dict",
      cascade = {CascadeType.PERSIST, CascadeType.REMOVE})
  private List<DictDetail> dictDetails;

  @NotBlank
  @Schema(name = "名称")
  private String name;

  @ApiModelProperty(value = "描述")
  private String description;
}
