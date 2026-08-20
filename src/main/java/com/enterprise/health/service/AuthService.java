package com.enterprise.health.service;

import java.util.Map;

public interface AuthService {
    Map<String, Object> login(String username, String password);
    void logout();
    Map<String, Object> me(Long userId);
    void changePassword(Long userId, String oldPassword, String newPassword);
}
