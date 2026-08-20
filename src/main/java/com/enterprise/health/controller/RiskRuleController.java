package com.enterprise.health.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.enterprise.health.common.result.Result;
import com.enterprise.health.entity.HealthRiskRule;
import com.enterprise.health.service.RiskRuleService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/risk-rules")
public class RiskRuleController {

    private final RiskRuleService riskRuleService;

    public RiskRuleController(RiskRuleService riskRuleService) {
        this.riskRuleService = riskRuleService;
    }

    @GetMapping
    public Result<?> list() {
        return Result.ok(riskRuleService.list());
    }

    @PostMapping
    @SaCheckPermission("risk:create")
    public Result<?> create(@RequestBody HealthRiskRule rule) {
        return Result.ok(riskRuleService.create(rule));
    }

    @PutMapping("/{id}")
    @SaCheckPermission("risk:update")
    public Result<?> update(@PathVariable Long id, @RequestBody HealthRiskRule rule) {
        return Result.ok(riskRuleService.update(id, rule));
    }

    @DeleteMapping("/{id}")
    @SaCheckPermission("risk:delete")
    public Result<Void> delete(@PathVariable Long id) {
        riskRuleService.delete(id);
        return Result.ok();
    }
}
