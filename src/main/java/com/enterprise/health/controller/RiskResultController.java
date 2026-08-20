package com.enterprise.health.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.stp.StpUtil;
import com.enterprise.health.common.result.Result;
import com.enterprise.health.service.RiskResultService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/risk-results")
public class RiskResultController {

    private final RiskResultService riskResultService;

    public RiskResultController(RiskResultService riskResultService) {
        this.riskResultService = riskResultService;
    }

    @GetMapping
    public Result<?> list(@RequestParam(defaultValue = "1") int page,
                          @RequestParam(defaultValue = "10") int size,
                          @RequestParam(required = false) Long deptId,
                          @RequestParam(required = false) String riskLevel) {
        return Result.ok(riskResultService.list(page, size, deptId, riskLevel));
    }

    @PostMapping("/assess/{recordId}")
    @SaCheckPermission("risk:assess")
    public Result<?> assess(@PathVariable Long recordId) {
        return Result.ok(riskResultService.assess(recordId));
    }

    @GetMapping("/mine")
    public Result<?> mine() {
        Long userId = StpUtil.getLoginIdAsLong();
        return Result.ok(riskResultService.mine(userId));
    }
}
