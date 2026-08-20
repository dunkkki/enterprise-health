package com.enterprise.health.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.enterprise.health.common.result.Result;
import com.enterprise.health.entity.ExamPackage;
import com.enterprise.health.service.ExamPackageService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/packages")
public class ExamPackageController {

    private final ExamPackageService packageService;

    public ExamPackageController(ExamPackageService packageService) {
        this.packageService = packageService;
    }

    @GetMapping
    public Result<?> list() {
        return Result.ok(packageService.list());
    }

    @GetMapping("/{id}")
    public Result<?> detail(@PathVariable Long id) {
        return Result.ok(packageService.detail(id));
    }

    @PostMapping
    @SaCheckPermission("package:create")
    public Result<?> create(@RequestBody ExamPackage pkg) {
        return Result.ok(packageService.create(pkg));
    }

    @PutMapping("/{id}")
    @SaCheckPermission("package:update")
    public Result<?> update(@PathVariable Long id, @RequestBody ExamPackage pkg) {
        return Result.ok(packageService.update(id, pkg));
    }

    @DeleteMapping("/{id}")
    @SaCheckPermission("package:delete")
    public Result<Void> delete(@PathVariable Long id) {
        packageService.delete(id);
        return Result.ok();
    }
}
