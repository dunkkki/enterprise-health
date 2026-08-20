package com.enterprise.health.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.enterprise.health.common.result.Result;
import com.enterprise.health.entity.FollowUpRecord;
import com.enterprise.health.service.FollowUpService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/follow-ups")
public class FollowUpController {

    private final FollowUpService followUpService;

    public FollowUpController(FollowUpService followUpService) {
        this.followUpService = followUpService;
    }

    @GetMapping
    public Result<?> list(@RequestParam(defaultValue = "1") int page,
                          @RequestParam(defaultValue = "10") int size,
                          @RequestParam(required = false) Long planId,
                          @RequestParam(required = false) Long userId) {
        return Result.ok(followUpService.list(page, size, planId, userId));
    }

    @PostMapping
    @SaCheckPermission("followup:create")
    public Result<?> create(@RequestBody FollowUpRecord record) {
        return Result.ok(followUpService.create(record));
    }

    @PutMapping("/{id}")
    @SaCheckPermission("followup:update")
    public Result<?> update(@PathVariable Long id, @RequestBody FollowUpRecord record) {
        return Result.ok(followUpService.update(id, record));
    }
}
