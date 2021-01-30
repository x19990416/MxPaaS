/**
 * create by Guo Limin on 2021/1/30.
 */
package com.github.x19990416.mxpaas.admin.modules.system.service.dto;

import com.github.x19990416.mxpaas.admin.common.base.BaseMapper;
import com.github.x19990416.mxpaas.admin.modules.system.domain.DictDetail;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", uses = {DictSmallMapper.class}, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface DictDetailMapper extends BaseMapper<DictDetailDto, DictDetail> {

}