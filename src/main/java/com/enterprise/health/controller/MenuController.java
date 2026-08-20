package com.enterprise.health.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.enterprise.health.common.result.Result;
import com.enterprise.health.entity.Menu;
import com.enterprise.health.service.MenuService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/menus")
public class MenuController {

    private final MenuService menuService;

    public MenuController(MenuService menuService) {
        this.menuService = menuService;
    }

    @GetMapping("/tree")
    @SaCheckPermission("menu:list")
    public Result<?> tree() {
        return Result.ok(menuService.tree());
    }

    @PostMapping
    @SaCheckPermission("menu:create")
    public Result<?> create(@RequestBody Menu menu) {
        return Result.ok(menuService.create(menu));
    }

    @PutMapping("/{id}")
    @SaCheckPermission("menu:update")
    public Result<?> update(@PathVariable Long id, @RequestBody Menu menu) {
        return Result.ok(menuService.update(id, menu));
    }

    @DeleteMapping("/{id}")
    @SaCheckPermission("menu:delete")
    public Result<Void> delete(@PathVariable Long id) {
        menuService.delete(id);
        return Result.ok();
    }
}
