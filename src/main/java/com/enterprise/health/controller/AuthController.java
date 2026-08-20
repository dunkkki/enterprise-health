package com.enterprise.health.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.enterprise.health.common.result.Result;
import com.enterprise.health.domain.LoginDTO;
import com.enterprise.health.domain.PasswordDTO;
import com.enterprise.health.service.AuthService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public Result<?> login(@RequestBody LoginDTO dto) {
        return Result.ok(authService.login(dto.username(), dto.password()));
    }

    @DeleteMapping("/logout")
    public Result<Void> logout() {
        authService.logout();
        return Result.ok();
    }

    @GetMapping("/me")
    public Result<?> me() {
        Long userId = StpUtil.getLoginIdAsLong();
        return Result.ok(authService.me(userId));
    }

    @PutMapping("/password")
    public Result<Void> changePassword(@RequestBody PasswordDTO dto) {
        Long userId = StpUtil.getLoginIdAsLong();
        authService.changePassword(userId, dto.oldPassword(), dto.newPassword());
        return Result.ok();
    }
}
