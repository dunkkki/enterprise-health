package com.enterprise.health.service;

import com.enterprise.health.entity.User;
import java.util.Map;

public interface UserService {
    Map<String, Object> list(int page, int size, String keyword, Long deptId);
    User create(User user);
    User update(Long id, User user);
    void delete(Long id);
    void toggleStatus(Long id);
    int importExcel(java.io.InputStream inputStream);
}
