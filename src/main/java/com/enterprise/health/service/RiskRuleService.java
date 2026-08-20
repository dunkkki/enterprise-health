package com.enterprise.health.service;

import com.enterprise.health.entity.HealthRiskRule;
import java.util.List;
import java.util.Map;

public interface RiskRuleService {
    List<Map<String, Object>> list();
    HealthRiskRule create(HealthRiskRule rule);
    HealthRiskRule update(Long id, HealthRiskRule rule);
    void delete(Long id);
}
