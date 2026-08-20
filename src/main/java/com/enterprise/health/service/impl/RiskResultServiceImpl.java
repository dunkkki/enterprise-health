package com.enterprise.health.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.enterprise.health.common.exception.BusinessException;
import com.enterprise.health.common.util.DataScopeUtil;
import com.enterprise.health.entity.ExamPackageItem;
import com.enterprise.health.entity.ExamRecord;
import com.enterprise.health.entity.ExamResultItem;
import com.enterprise.health.entity.ExamSchedule;
import com.enterprise.health.entity.HealthRiskResult;
import com.enterprise.health.entity.HealthRiskRule;
import com.enterprise.health.entity.User;
import com.enterprise.health.mapper.ExamPackageItemMapper;
import com.enterprise.health.mapper.ExamRecordMapper;
import com.enterprise.health.mapper.ExamResultItemMapper;
import com.enterprise.health.mapper.ExamScheduleMapper;
import com.enterprise.health.mapper.HealthRiskResultMapper;
import com.enterprise.health.mapper.HealthRiskRuleMapper;
import com.enterprise.health.mapper.UserMapper;
import com.enterprise.health.service.RiskResultService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Comparator;
import java.util.stream.Collectors;

@Service
public class RiskResultServiceImpl implements RiskResultService {

    private final HealthRiskRuleMapper ruleMapper;
    private final HealthRiskResultMapper resultMapper;
    private final ExamRecordMapper recordMapper;
    private final ExamResultItemMapper resultItemMapper;
    private final ExamPackageItemMapper packageItemMapper;
    private final ExamScheduleMapper scheduleMapper;
    private final UserMapper userMapper;
    private final ObjectMapper objectMapper;
    private final DataScopeUtil dataScopeUtil;

    public RiskResultServiceImpl(HealthRiskRuleMapper ruleMapper, HealthRiskResultMapper resultMapper,
                                  ExamRecordMapper recordMapper, ExamResultItemMapper resultItemMapper,
                                  ExamPackageItemMapper packageItemMapper, ExamScheduleMapper scheduleMapper,
                                  UserMapper userMapper, ObjectMapper objectMapper,
                                  DataScopeUtil dataScopeUtil) {
        this.ruleMapper = ruleMapper;
        this.resultMapper = resultMapper;
        this.recordMapper = recordMapper;
        this.resultItemMapper = resultItemMapper;
        this.packageItemMapper = packageItemMapper;
        this.scheduleMapper = scheduleMapper;
        this.userMapper = userMapper;
        this.objectMapper = objectMapper;
        this.dataScopeUtil = dataScopeUtil;
    }

    @Override
    public Map<String, Object> list(int page, int size, Long deptId, String riskLevel) {
        LambdaQueryWrapper<HealthRiskResult> qw = new LambdaQueryWrapper<>();

        int scope = dataScopeUtil.getDataScope();
        if (scope == 1) {
            List<Long> deptUserIds = getDeptUserIds(dataScopeUtil.getCurrentDeptId());
            if (deptUserIds.isEmpty()) return emptyResult(page, size);
            qw.in(HealthRiskResult::getUserId, deptUserIds);
        } else if (scope == 2) {
            qw.eq(HealthRiskResult::getUserId, StpUtil.getLoginIdAsLong());
        } else if (deptId != null) {
            List<Long> deptUserIds = getDeptUserIds(deptId);
            if (!deptUserIds.isEmpty()) qw.in(HealthRiskResult::getUserId, deptUserIds);
        }

        if (riskLevel != null && !riskLevel.isEmpty()) qw.eq(HealthRiskResult::getRiskLevel, riskLevel);
        qw.orderByDesc(HealthRiskResult::getAssessedAt);

        Page<HealthRiskResult> result = resultMapper.selectPage(new Page<>(page, size), qw);

        List<Map<String, Object>> records = new ArrayList<>();
        for (HealthRiskResult r : result.getRecords()) {
            Map<String, Object> map = toMap(r);
            User user = userMapper.selectById(r.getUserId());
            map.put("userName", user != null ? user.getRealName() : "");
            map.put("employeeNo", user != null ? user.getEmployeeNo() : "");
            ExamRecord exam = recordMapper.selectById(r.getRecordId());
            if (exam != null) {
                ExamSchedule schedule = scheduleMapper.selectById(exam.getScheduleId());
                map.put("scheduleTitle", schedule != null ? schedule.getTitle() : "");
                map.put("examDate", exam.getExamDate() != null ? exam.getExamDate().toString() : null);
            }
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
    @Transactional
    public Map<String, Object> assess(Long recordId) {
        ExamRecord record = recordMapper.selectById(recordId);
        if (record == null) throw new BusinessException(400, "体检记录不存在");
        if (record.getStatus() == 0) throw new BusinessException(400, "该体检尚未录入结果");

        resultMapper.delete(new LambdaQueryWrapper<HealthRiskResult>()
                .eq(HealthRiskResult::getRecordId, recordId));

        List<ExamResultItem> items = resultItemMapper.selectList(
                new LambdaQueryWrapper<ExamResultItem>().eq(ExamResultItem::getRecordId, recordId));
        if (items.isEmpty()) throw new BusinessException(400, "该体检没有检测结果数据");

        List<HealthRiskRule> allRules = ruleMapper.selectList(
                new LambdaQueryWrapper<HealthRiskRule>().eq(HealthRiskRule::getStatus, 1));

        BigDecimal totalScore = BigDecimal.ZERO;
        List<Map<String, Object>> hitDetails = new ArrayList<>();

        for (ExamResultItem item : items) {
            ExamPackageItem pi = packageItemMapper.selectById(item.getPackageItemId());
            if (pi == null) continue;

            List<HealthRiskRule> matchedRules = allRules.stream()
                    .filter(r -> r.getPackageItemId().equals(item.getPackageItemId()))
                    .collect(Collectors.toList());

            for (HealthRiskRule rule : matchedRules) {
                if (evaluateCondition(rule, item, pi)) {
                    BigDecimal ruleScore = BigDecimal.valueOf(rule.getScore())
                            .multiply(rule.getWeight() != null ? rule.getWeight() : BigDecimal.ONE);
                    totalScore = totalScore.add(ruleScore);

                    Map<String, Object> hit = new LinkedHashMap<>();
                    hit.put("ruleName", rule.getRuleName());
                    hit.put("itemName", pi.getItemName());
                    hit.put("itemValue", item.getItemValue());
                    hit.put("conditionType", rule.getConditionType());
                    hit.put("thresholdValue", rule.getThresholdValue());
                    hit.put("score", ruleScore);
                    hit.put("riskLevel", rule.getRiskLevel());
                    hitDetails.add(hit);
                }
            }
        }

        String riskLevel;
        if (totalScore.compareTo(new BigDecimal("50")) > 0) riskLevel = "高";
        else if (totalScore.compareTo(new BigDecimal("20")) >= 0) riskLevel = "中";
        else riskLevel = "低";

        HealthRiskResult result = new HealthRiskResult();
        result.setUserId(record.getUserId());
        result.setRecordId(recordId);
        result.setTotalScore(totalScore);
        result.setRiskLevel(riskLevel);
        try {
            result.setDetailJson(objectMapper.writeValueAsString(hitDetails));
        } catch (JsonProcessingException e) {
            result.setDetailJson("[]");
        }
        result.setAssessedAt(LocalDateTime.now());
        resultMapper.insert(result);

        Map<String, Object> response = toMap(result);
        response.put("hitDetails", hitDetails);
        return response;
    }

    @Override
    public Map<String, Object> mine(Long userId) {
        List<HealthRiskResult> results = resultMapper.selectList(
                new LambdaQueryWrapper<HealthRiskResult>().eq(HealthRiskResult::getUserId, userId)
                        .orderByDesc(HealthRiskResult::getAssessedAt));

        List<Map<String, Object>> list = new ArrayList<>();
        for (HealthRiskResult r : results) {
            Map<String, Object> m = toMap(r);
            ExamRecord exam = recordMapper.selectById(r.getRecordId());
            if (exam != null) {
                ExamSchedule schedule = scheduleMapper.selectById(exam.getScheduleId());
                m.put("scheduleTitle", schedule != null ? schedule.getTitle() : "");
            }
            list.add(m);
        }
        Map<String, Object> result = new HashMap<>();
        result.put("records", list);
        result.put("total", list.size());
        return result;
    }

    private boolean evaluateCondition(HealthRiskRule rule, ExamResultItem item, ExamPackageItem pi) {
        String value = item.getItemValue();
        if (value == null || value.isBlank()) return false;

        switch (rule.getConditionType()) {
            case "gt": {
                BigDecimal v = parseNumber(value);
                BigDecimal t = parseNumber(rule.getThresholdValue());
                return v != null && t != null && v.compareTo(t) > 0;
            }
            case "lt": {
                BigDecimal v = parseNumber(value);
                BigDecimal t = parseNumber(rule.getThresholdValue());
                return v != null && t != null && v.compareTo(t) < 0;
            }
            case "out_of_range": {
                BigDecimal v = parseNumber(value);
                if (v == null) return false;
                if (pi.getRefMin() != null && v.compareTo(pi.getRefMin()) < 0) return true;
                if (pi.getRefMax() != null && v.compareTo(pi.getRefMax()) > 0) return true;
                return false;
            }
            case "equals":
                return value.equals(rule.getThresholdValue());
            default:
                return false;
        }
    }

    private BigDecimal parseNumber(String s) {
        try {
            return new BigDecimal(s);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private List<Long> getDeptUserIds(Long deptId) {
        return userMapper.selectList(
                new LambdaQueryWrapper<User>().eq(User::getDeptId, deptId))
                .stream().map(User::getId).collect(Collectors.toList());
    }

    private Map<String, Object> emptyResult(int page, int size) {
        Map<String, Object> empty = new HashMap<>();
        empty.put("records", Collections.emptyList());
        empty.put("total", 0L);
        empty.put("page", (long) page);
        empty.put("size", (long) size);
        return empty;
    }

    private Map<String, Object> toMap(HealthRiskResult r) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", r.getId().toString());
        map.put("userId", r.getUserId() != null ? r.getUserId().toString() : null);
        map.put("recordId", r.getRecordId() != null ? r.getRecordId().toString() : null);
        map.put("totalScore", r.getTotalScore());
        map.put("riskLevel", r.getRiskLevel());
        map.put("detailJson", r.getDetailJson());
        map.put("assessedAt", r.getAssessedAt() != null ? r.getAssessedAt().toString() : null);
        return map;
    }
}
