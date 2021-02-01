/**
 * create by Guo Limin on 2021/1/31.
 */
package com.github.x19990416.mxpaas.admin.modules.system.rest;

import com.github.x19990416.mxpaas.admin.common.exception.BadRequestException;
import com.github.x19990416.mxpaas.admin.modules.system.domain.Job;
import com.github.x19990416.mxpaas.admin.modules.system.service.JobService;
import com.github.x19990416.mxpaas.admin.modules.system.service.dto.JobQueryCriteria;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Set;

@RestController
@RequiredArgsConstructor
@Tag(name = "系统：岗位管理")
@RequestMapping("/api/job")
public class JobController {

    private final JobService jobService;
    private static final String ENTITY_NAME = "job";

    @Operation(method = "导出岗位数据")
    @GetMapping(value = "/download")
    @PreAuthorize("@_sys.check('job:list')")
    public void download(HttpServletResponse response, JobQueryCriteria criteria) throws IOException {
        jobService.download(jobService.queryAll(criteria), response);
    }

    @Operation(method = "查询岗位")
    @GetMapping
    @PreAuthorize("@_sys.check('job:list','user:list')")
    public ResponseEntity<Object> query(JobQueryCriteria criteria, Pageable pageable) {
        return new ResponseEntity<>(jobService.queryAll(criteria, pageable), HttpStatus.OK);
    }

    @Operation(method = "新增岗位")
    @PostMapping
    @PreAuthorize("@_sys.check('job:add')")
    public ResponseEntity<Object> create(@Validated @RequestBody Job resources) {
        if (resources.getId() != null) {
            throw new BadRequestException("A new " + ENTITY_NAME + " cannot already have an ID");
        }
        jobService.create(resources);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @Operation(method = "修改岗位")
    @PutMapping
    @PreAuthorize("@_sys.check('job:edit')")
    public ResponseEntity<Object> update(@Validated(Job.Update.class) @RequestBody Job resources) {
        jobService.update(resources);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Operation(method = "删除岗位")
    @DeleteMapping
    @PreAuthorize("@_sys.check('job:del')")
    public ResponseEntity<Object> delete(@RequestBody Set<Long> ids) {
        // 验证是否被用户关联
        jobService.verification(ids);
        jobService.delete(ids);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}