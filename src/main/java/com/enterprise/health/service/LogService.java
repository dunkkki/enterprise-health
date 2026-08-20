package com.enterprise.health.service;

import java.util.Map;

public interface LogService {
    Map<String, Object> operationLogs(int page, int size, String username, String module);
    Map<String, Object> loginLogs(int page, int size, String username, Integer status);
}
