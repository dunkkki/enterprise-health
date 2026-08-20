package com.enterprise.health.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.enterprise.health.common.result.Result;
import com.enterprise.health.service.DashboardService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/summary")
    @SaCheckPermission("dashboard:list")
    public Result<?> summary() {
        return Result.ok(dashboardService.summary());
    }

    @GetMapping("/risk-distribution")
    @SaCheckPermission("dashboard:list")
    public Result<?> riskDistribution() {
        return Result.ok(dashboardService.riskDistribution());
    }

    @GetMapping("/dept-ranking")
    @SaCheckPermission("dashboard:list")
    public Result<?> deptRanking() {
        return Result.ok(dashboardService.deptRanking());
    }

    @GetMapping("/exam-trend")
    @SaCheckPermission("dashboard:list")
    public Result<?> examTrend() {
        return Result.ok(dashboardService.examTrend());
    }

    @GetMapping("/intervention-stats")
    @SaCheckPermission("dashboard:list")
    public Result<?> interventionStats() {
        return Result.ok(dashboardService.interventionStats());
    }
}
