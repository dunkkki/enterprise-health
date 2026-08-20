package com.enterprise.health.service;

import java.util.Map;

public interface DashboardService {
    Map<String, Object> summary();
    Map<String, Object> riskDistribution();
    Map<String, Object> deptRanking();
    Map<String, Object> examTrend();
    Map<String, Object> interventionStats();
}
