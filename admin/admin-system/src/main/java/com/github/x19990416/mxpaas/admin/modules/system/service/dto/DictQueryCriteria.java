/**
 * create by Guo Limin on 2021/1/30.
 */
package com.github.x19990416.mxpaas.admin.modules.system.service.dto;

import com.github.x19990416.mxpaas.admin.common.annotation.Query;
import lombok.Data;

@Data
public class DictQueryCriteria {

    @Query(blurry = "name,description")
    private String blurry;
}
