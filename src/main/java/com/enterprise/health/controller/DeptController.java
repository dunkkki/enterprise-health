package com.enterprise.health.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.enterprise.health.common.result.Result;
import com.enterprise.health.entity.Department;
import com.enterprise.health.service.DeptService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/depts")
public class DeptController {

    private final DeptService deptService;

    public DeptController(DeptService deptService) {
        this.deptService = deptService;
    }

    @GetMapping("/tree")
    public Result<?> tree() {
        return Result.ok(deptService.tree());
    }

    @PostMapping
    @SaCheckPermission("dept:create")
    public Result<?> create(@RequestBody Department dept) {
        return Result.ok(deptService.create(dept));
    }

    @PutMapping("/{id}")
    @SaCheckPermission("dept:update")
    public Result<?> update(@PathVariable Long id, @RequestBody Department dept) {
        return Result.ok(deptService.update(id, dept));
    }

    @DeleteMapping("/{id}")
    @SaCheckPermission("dept:delete")
    public Result<Void> delete(@PathVariable Long id) {
        deptService.delete(id);
        return Result.ok();
    }
}
