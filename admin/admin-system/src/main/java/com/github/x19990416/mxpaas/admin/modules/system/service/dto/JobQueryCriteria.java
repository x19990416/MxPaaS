/** create by Guo Limin on 2021/1/30. */
package com.github.x19990416.mxpaas.admin.modules.system.service.dto;

import com.github.x19990416.mxpaas.admin.common.annotation.Query;
import lombok.Data;

import java.sql.Timestamp;
import java.util.List;

@Data
public class JobQueryCriteria {

  @Query(type = Query.Type.INNER_LIKE)
  private String name;

  @Query private Boolean enabled;

  @Query(type = Query.Type.BETWEEN)
  private List<Timestamp> createTime;
}
