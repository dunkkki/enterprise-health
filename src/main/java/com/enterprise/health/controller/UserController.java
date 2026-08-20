package com.enterprise.health.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.enterprise.health.common.result.Result;
import com.enterprise.health.entity.User;
import com.enterprise.health.service.UserService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public Result<?> list(@RequestParam(defaultValue = "1") int page,
                          @RequestParam(defaultValue = "10") int size,
                          @RequestParam(required = false) String keyword,
                          @RequestParam(required = false) Long deptId) {
        return Result.ok(userService.list(page, size, keyword, deptId));
    }

    @PostMapping
    @SaCheckPermission("user:create")
    public Result<?> create(@RequestBody User user) {
        return Result.ok(userService.create(user));
    }

    @PutMapping("/{id}")
    @SaCheckPermission("user:update")
    public Result<?> update(@PathVariable Long id, @RequestBody User user) {
        return Result.ok(userService.update(id, user));
    }

    @DeleteMapping("/{id}")
    @SaCheckPermission("user:delete")
    public Result<Void> delete(@PathVariable Long id) {
        userService.delete(id);
        return Result.ok();
    }

    @PutMapping("/{id}/status")
    @SaCheckPermission("user:update")
    public Result<Void> toggleStatus(@PathVariable Long id) {
        userService.toggleStatus(id);
        return Result.ok();
    }

    @PostMapping("/import")
    @SaCheckPermission("user:import")
    public Result<?> importExcel(@RequestParam MultipartFile file) {
        try {
            int count = userService.importExcel(file.getInputStream());
            return Result.ok("成功导入 " + count + " 条数据");
        } catch (Exception e) {
            return Result.fail(400, "导入失败: " + e.getMessage());
        }
    }
}
