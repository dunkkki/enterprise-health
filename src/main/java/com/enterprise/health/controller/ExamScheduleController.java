package com.enterprise.health.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.enterprise.health.common.result.Result;
import com.enterprise.health.domain.ScheduleDTO;
import com.enterprise.health.service.ExamScheduleService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/schedules")
public class ExamScheduleController {

    private final ExamScheduleService scheduleService;

    public ExamScheduleController(ExamScheduleService scheduleService) {
        this.scheduleService = scheduleService;
    }

    @GetMapping
    public Result<?> list(@RequestParam(defaultValue = "1") int page,
                          @RequestParam(defaultValue = "10") int size) {
        return Result.ok(scheduleService.list(page, size));
    }

    @PostMapping
    @SaCheckPermission("schedule:create")
    public Result<?> create(@RequestBody ScheduleDTO dto) {
        return Result.ok(scheduleService.create(dto));
    }

    @PutMapping("/{id}")
    @SaCheckPermission("schedule:update")
    public Result<?> update(@PathVariable Long id, @RequestBody ScheduleDTO dto) {
        return Result.ok(scheduleService.update(id, dto));
    }

    @PutMapping("/{id}/status")
    @SaCheckPermission("schedule:update")
    public Result<Void> changeStatus(@PathVariable Long id, @RequestParam Integer status) {
        scheduleService.changeStatus(id, status);
        return Result.ok();
    }
}
