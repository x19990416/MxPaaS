/**
 * create by Guo Limin on 2021/1/31.
 */
package com.github.x19990416.mxpaas.admin.modules.system.rest;

import com.github.x19990416.mxpaas.admin.common.annotation.Limit;
import com.github.x19990416.mxpaas.admin.common.annotation.controller.AnonymousGetMapping;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.atomic.AtomicInteger;

@RestController
@RequestMapping("/api/limit")
@Tag(name = "系统：限流测试管理")
public class LimitController {

    private static final AtomicInteger ATOMIC_INTEGER = new AtomicInteger();

    /**
     * 测试限流注解，下面配置说明该接口 60秒内最多只能访问 10次，保存到redis的键名为 limit_test，
     */
    @AnonymousGetMapping
    @Operation(method = "测试")
    @Limit(key = "test", period = 60, count = 10, name = "testLimit", prefix = "limit")
    public int test() {
        return ATOMIC_INTEGER.incrementAndGet();
    }
}