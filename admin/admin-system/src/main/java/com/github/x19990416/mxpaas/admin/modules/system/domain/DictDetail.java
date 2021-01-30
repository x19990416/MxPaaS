/**
 * create by Guo Limin on 2021/1/30.
 */
package com.github.x19990416.mxpaas.admin.modules.system.domain;

import com.github.x19990416.mxpaas.admin.common.base.BaseEntity;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Entity
@EqualsAndHashCode(callSuper = true)
@Table(name = "sys_dict_detail")
@Data
public class DictDetail extends BaseEntity implements Serializable {
    @Id
    @Column(name = "detail_id")
    @NotNull(groups = Update.class)
    @Schema(name = "ID", hidden = true)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JoinColumn(name = "dict_id")
    @ManyToOne(fetch=FetchType.LAZY)
    @Schema(name = "字典", hidden = true)
    private Dict dict;

    @ApiModelProperty(value = "字典标签")
    private String label;

    @ApiModelProperty(value = "字典值")
    private String value;

    @ApiModelProperty(value = "排序")
    private Integer dictSort = 999;
}
