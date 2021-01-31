/**
 * create by Guo Limin on 2021/1/31.
 */
package com.github.x19990416.mxpaas.admin.modules.system.service;

import com.github.x19990416.mxpaas.admin.modules.system.domain.vo.EmailVo;

public interface VerifyService {
    EmailVo sendEmail(String email, String key);


    /**
     * 验证
     * @param code /
     * @param key /
     */
    void validated(String key, String code);
}
