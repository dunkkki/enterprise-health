package com.enterprise.health.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.enterprise.health.common.result.Result;
import com.enterprise.health.domain.RoleMenuDTO;
import com.enterprise.health.entity.Role;
import com.enterprise.health.service.RoleService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/roles")
public class RoleController {

    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @GetMapping
    @SaCheckPermission("role:list")
    public Result<?> list() {
        return Result.ok(roleService.list());
    }

    @PostMapping
    @SaCheckPermission("role:create")
    public Result<?> create(@RequestBody Role role) {
        return Result.ok(roleService.create(role));
    }

    @PutMapping("/{id}")
    @SaCheckPermission("role:update")
    public Result<?> update(@PathVariable Long id, @RequestBody Role role) {
        return Result.ok(roleService.update(id, role));
    }

    @DeleteMapping("/{id}")
    @SaCheckPermission("role:delete")
    public Result<Void> delete(@PathVariable Long id) {
        roleService.delete(id);
        return Result.ok();
    }

    @GetMapping("/{id}/menus")
    @SaCheckPermission("role:list")
    public Result<?> getMenus(@PathVariable Long id) {
        return Result.ok(roleService.getMenus(id));
    }

    @PutMapping("/{id}/menus")
    @SaCheckPermission("role:update")
    public Result<Void> assignMenus(@PathVariable Long id, @RequestBody RoleMenuDTO dto) {
        roleService.assignMenus(id, dto.menuIds());
        return Result.ok();
    }
}
