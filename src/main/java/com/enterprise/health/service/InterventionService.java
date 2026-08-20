package com.enterprise.health.service;

import com.enterprise.health.entity.InterventionPlan;
import java.util.Map;

public interface InterventionService {
    Map<String, Object> list(int page, int size, String type, Integer status);
    InterventionPlan getById(Long id);
    InterventionPlan create(InterventionPlan plan);
    InterventionPlan update(Long id, InterventionPlan update);
    void delete(Long id);
    void changeStatus(Long id, Integer status);
}
