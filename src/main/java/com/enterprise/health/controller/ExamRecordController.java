package com.enterprise.health.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.stp.StpUtil;
import com.enterprise.health.common.result.Result;
import com.enterprise.health.domain.ExamResultDTO;
import com.enterprise.health.service.ExamRecordService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/records")
public class ExamRecordController {

    private final ExamRecordService recordService;

    public ExamRecordController(ExamRecordService recordService) {
        this.recordService = recordService;
    }

    @GetMapping
    public Result<?> list(@RequestParam(defaultValue = "1") int page,
                          @RequestParam(defaultValue = "10") int size,
                          @RequestParam(required = false) Long scheduleId,
                          @RequestParam(required = false) Long deptId,
                          @RequestParam(required = false) Integer status) {
        return Result.ok(recordService.list(page, size, scheduleId, deptId, status));
    }

    @GetMapping("/{id}")
    public Result<?> detail(@PathVariable Long id) {
        return Result.ok(recordService.detail(id));
    }

    @PostMapping
    @SaCheckPermission("record:create")
    public Result<Void> enterResult(@RequestBody ExamResultDTO dto) {
        recordService.enterResult(dto);
        return Result.ok();
    }

    @GetMapping("/mine")
    public Result<?> mine() {
        Long userId = StpUtil.getLoginIdAsLong();
        return Result.ok(recordService.mine(userId));
    }

    @GetMapping("/{id}/print")
    public Result<?> print(@PathVariable Long id) {
        return Result.ok(recordService.detail(id));
    }
}
