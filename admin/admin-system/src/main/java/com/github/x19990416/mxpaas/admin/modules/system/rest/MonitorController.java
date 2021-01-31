/** create by Guo Limin on 2021/1/31. */
package com.github.x19990416.mxpaas.admin.modules.system.rest;

import com.github.x19990416.mxpaas.admin.modules.system.service.MonitorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Tag(name = "系统-服务监控管理")
@RequestMapping("/api/monitor")
public class MonitorController {

  private final MonitorService serverService;

  @GetMapping
  @Operation(method = "查询服务监控")
  @PreAuthorize("@_sys.check('monitor:list')")
  public ResponseEntity<Object> query() {
    return new ResponseEntity<>(serverService.getServers(), HttpStatus.OK);
  }
}
