package com.enterprise.health.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.enterprise.health.entity.LoginLog;
import com.enterprise.health.entity.OperationLog;
import com.enterprise.health.mapper.LoginLogMapper;
import com.enterprise.health.mapper.OperationLogMapper;
import com.enterprise.health.service.LogService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Comparator;
import java.util.stream.Collectors;

@Service
public class LogServiceImpl implements LogService {

    private final OperationLogMapper operationLogMapper;
    private final LoginLogMapper loginLogMapper;

    public LogServiceImpl(OperationLogMapper operationLogMapper, LoginLogMapper loginLogMapper) {
        this.operationLogMapper = operationLogMapper;
        this.loginLogMapper = loginLogMapper;
    }

    @Override
    public Map<String, Object> operationLogs(int page, int size, String username, String module) {
        LambdaQueryWrapper<OperationLog> qw = new LambdaQueryWrapper<>();
        if (username != null && !username.isEmpty()) qw.like(OperationLog::getUsername, username);
        if (module != null && !module.isEmpty()) qw.eq(OperationLog::getModule, module);
        qw.orderByDesc(OperationLog::getCreatedAt);

        Page<OperationLog> result = operationLogMapper.selectPage(new Page<>(page, size), qw);
        Map<String, Object> data = new HashMap<>();
        data.put("records", result.getRecords());
        data.put("total", result.getTotal());
        data.put("page", result.getCurrent());
        data.put("size", result.getSize());
        return data;
    }

    @Override
    public Map<String, Object> loginLogs(int page, int size, String username, Integer status) {
        LambdaQueryWrapper<LoginLog> qw = new LambdaQueryWrapper<>();
        if (username != null && !username.isEmpty()) qw.like(LoginLog::getUsername, username);
        if (status != null) qw.eq(LoginLog::getStatus, status);
        qw.orderByDesc(LoginLog::getCreatedAt);

        Page<LoginLog> result = loginLogMapper.selectPage(new Page<>(page, size), qw);
        Map<String, Object> data = new HashMap<>();
        data.put("records", result.getRecords());
        data.put("total", result.getTotal());
        data.put("page", result.getCurrent());
        data.put("size", result.getSize());
        return data;
    }
}
