/**
 * create by Guo Limin on 2021/1/29.
 */
package com.github.x19990416.mxpaas.admin.modules.system.service.dto;

import com.github.x19990416.mxpaas.admin.common.annotation.DataPermission;
import com.github.x19990416.mxpaas.admin.common.annotation.Query;
import lombok.Data;

import java.sql.Timestamp;
import java.util.List;

@Data
@DataPermission(fieldName = "id")
public class DeptQueryCriteria {
    @Query(type = Query.Type.INNER_LIKE)
    private String name;

    @Query
    private Boolean enabled;

    @Query
    private Long pid;

    @Query(type = Query.Type.IS_NULL, propName = "pid")
    private Boolean pidIsNull;

    @Query(type = Query.Type.BETWEEN)
    private List<Timestamp> createTime;
}
