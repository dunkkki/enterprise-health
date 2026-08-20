package com.enterprise.health.service;

import com.enterprise.health.entity.Department;
import java.util.List;
import java.util.Map;

public interface DeptService {
    List<Map<String, Object>> tree();
    Department create(Department dept);
    Department update(Long id, Department dept);
    void delete(Long id);
}
