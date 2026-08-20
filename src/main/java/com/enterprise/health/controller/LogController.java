package com.enterprise.health.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.enterprise.health.common.result.Result;
import com.enterprise.health.service.LogService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/logs")
public class LogController {

    private final LogService logService;

    public LogController(LogService logService) {
        this.logService = logService;
    }

    @GetMapping("/operation")
    @SaCheckPermission("log:list")
    public Result<?> operationLogs(@RequestParam(defaultValue = "1") int page,
                                   @RequestParam(defaultValue = "10") int size,
                                   @RequestParam(required = false) String username,
                                   @RequestParam(required = false) String module) {
        return Result.ok(logService.operationLogs(page, size, username, module));
    }

    @GetMapping("/login")
    @SaCheckPermission("log:list")
    public Result<?> loginLogs(@RequestParam(defaultValue = "1") int page,
                               @RequestParam(defaultValue = "10") int size,
                               @RequestParam(required = false) String username,
                               @RequestParam(required = false) Integer status) {
        return Result.ok(logService.loginLogs(page, size, username, status));
    }
}
