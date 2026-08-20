package com.enterprise.health.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.enterprise.health.common.exception.BusinessException;
import com.enterprise.health.common.util.DataScopeUtil;
import com.enterprise.health.entity.InterventionParticipant;
import com.enterprise.health.entity.InterventionPlan;
import com.enterprise.health.entity.User;
import com.enterprise.health.mapper.InterventionParticipantMapper;
import com.enterprise.health.mapper.InterventionPlanMapper;
import com.enterprise.health.mapper.UserMapper;
import com.enterprise.health.service.InterventionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Comparator;
import java.util.stream.Collectors;

@Service
public class InterventionServiceImpl implements InterventionService {

    private final InterventionPlanMapper planMapper;
    private final InterventionParticipantMapper participantMapper;
    private final UserMapper userMapper;
    private final DataScopeUtil dataScopeUtil;

    public InterventionServiceImpl(InterventionPlanMapper planMapper,
                                    InterventionParticipantMapper participantMapper,
                                    UserMapper userMapper,
                                    DataScopeUtil dataScopeUtil) {
        this.planMapper = planMapper;
        this.participantMapper = participantMapper;
        this.userMapper = userMapper;
        this.dataScopeUtil = dataScopeUtil;
    }

    @Override
    public Map<String, Object> list(int page, int size, String type, Integer status) {
        LambdaQueryWrapper<InterventionPlan> qw = new LambdaQueryWrapper<>();

        int scope = dataScopeUtil.getDataScope();
        if (scope == 2) {
            List<Long> planIds = participantMapper.selectList(
                    new LambdaQueryWrapper<InterventionParticipant>()
                            .eq(InterventionParticipant::getUserId, StpUtil.getLoginIdAsLong()))
                    .stream().map(InterventionParticipant::getPlanId).distinct().toList();
            if (planIds.isEmpty()) return emptyResult(page, size);
            qw.in(InterventionPlan::getId, planIds);
        }

        if (type != null && !type.isEmpty()) qw.eq(InterventionPlan::getType, type);
        if (status != null) qw.eq(InterventionPlan::getStatus, status);
        qw.orderByDesc(InterventionPlan::getCreatedAt);

        Page<InterventionPlan> result = planMapper.selectPage(new Page<>(page, size), qw);

        List<Map<String, Object>> records = new ArrayList<>();
        for (InterventionPlan plan : result.getRecords()) {
            Map<String, Object> map = toMap(plan);
            User executor = userMapper.selectById(plan.getExecutorId());
            map.put("executorName", executor != null ? executor.getRealName() : "");
            long participantCount = participantMapper.selectCount(
                    new LambdaQueryWrapper<InterventionParticipant>()
                            .eq(InterventionParticipant::getPlanId, plan.getId()));
            map.put("participantCount", participantCount);
            records.add(map);
        }

        Map<String, Object> data = new HashMap<>();
        data.put("records", records);
        data.put("total", result.getTotal());
        data.put("page", result.getCurrent());
        data.put("size", result.getSize());
        return data;
    }

    @Override
    public InterventionPlan getById(Long id) {
        return planMapper.selectById(id);
    }

    @Override
    @Transactional
    public InterventionPlan create(InterventionPlan plan) {
        plan.setCreatedBy(StpUtil.getLoginIdAsLong());
        plan.setStatus(0);
        planMapper.insert(plan);
        return plan;
    }

    @Override
    public InterventionPlan update(Long id, InterventionPlan update) {
        InterventionPlan existing = planMapper.selectById(id);
        if (existing == null) throw new BusinessException(400, "干预计划不存在");
        if (update.getTitle() != null) existing.setTitle(update.getTitle());
        if (update.getType() != null) existing.setType(update.getType());
        if (update.getDescription() != null) existing.setDescription(update.getDescription());
        if (update.getStartDate() != null) existing.setStartDate(update.getStartDate());
        if (update.getEndDate() != null) existing.setEndDate(update.getEndDate());
        if (update.getExecutorId() != null) existing.setExecutorId(update.getExecutorId());
        planMapper.updateById(existing);
        return existing;
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (planMapper.selectById(id) == null) throw new BusinessException(400, "干预计划不存在");
        participantMapper.delete(new LambdaQueryWrapper<InterventionParticipant>()
                .eq(InterventionParticipant::getPlanId, id));
        planMapper.deleteById(id);
    }

    @Override
    public void changeStatus(Long id, Integer status) {
        InterventionPlan plan = planMapper.selectById(id);
        if (plan == null) throw new BusinessException(400, "干预计划不存在");
        plan.setStatus(status);
        planMapper.updateById(plan);
    }

    private Map<String, Object> toMap(InterventionPlan p) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", p.getId().toString());
        map.put("title", p.getTitle());
        map.put("type", p.getType());
        map.put("description", p.getDescription());
        map.put("startDate", p.getStartDate() != null ? p.getStartDate().toString() : null);
        map.put("endDate", p.getEndDate() != null ? p.getEndDate().toString() : null);
        map.put("executorId", p.getExecutorId() != null ? p.getExecutorId().toString() : null);
        map.put("status", p.getStatus());
        map.put("createdBy", p.getCreatedBy() != null ? p.getCreatedBy().toString() : null);
        map.put("createdAt", p.getCreatedAt() != null ? p.getCreatedAt().toString() : null);
        return map;
    }

    private Map<String, Object> emptyResult(int page, int size) {
        Map<String, Object> empty = new HashMap<>();
        empty.put("records", Collections.emptyList());
        empty.put("total", 0L);
        empty.put("page", (long) page);
        empty.put("size", (long) size);
        return empty;
    }
}
