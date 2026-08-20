package com.enterprise.health.service;

import java.util.Map;

public interface RiskResultService {
    Map<String, Object> list(int page, int size, Long deptId, String riskLevel);
    Map<String, Object> assess(Long recordId);
    Map<String, Object> mine(Long userId);
}
