package com.enterprise.health.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.enterprise.health.common.util.DataScopeUtil;
import com.enterprise.health.entity.Department;
import com.enterprise.health.entity.ExamRecord;
import com.enterprise.health.entity.ExamSchedule;
import com.enterprise.health.entity.HealthRiskResult;
import com.enterprise.health.entity.InterventionParticipant;
import com.enterprise.health.entity.InterventionPlan;
import com.enterprise.health.entity.User;
import com.enterprise.health.mapper.DepartmentMapper;
import com.enterprise.health.mapper.ExamRecordMapper;
import com.enterprise.health.mapper.ExamScheduleMapper;
import com.enterprise.health.mapper.HealthRiskResultMapper;
import com.enterprise.health.mapper.InterventionParticipantMapper;
import com.enterprise.health.mapper.InterventionPlanMapper;
import com.enterprise.health.mapper.UserMapper;
import com.enterprise.health.service.DashboardService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DashboardServiceImpl implements DashboardService {

    private final UserMapper userMapper;
    private final DepartmentMapper deptMapper;
    private final ExamScheduleMapper scheduleMapper;
    private final ExamRecordMapper recordMapper;
    private final HealthRiskResultMapper riskResultMapper;
    private final InterventionPlanMapper interventionMapper;
    private final InterventionParticipantMapper participantMapper;
    private final DataScopeUtil dataScopeUtil;

    public DashboardServiceImpl(UserMapper userMapper, DepartmentMapper deptMapper,
                                 ExamScheduleMapper scheduleMapper, ExamRecordMapper recordMapper,
                                 HealthRiskResultMapper riskResultMapper,
                                 InterventionPlanMapper interventionMapper,
                                 InterventionParticipantMapper participantMapper,
                                 DataScopeUtil dataScopeUtil) {
        this.userMapper = userMapper;
        this.deptMapper = deptMapper;
        this.scheduleMapper = scheduleMapper;
        this.recordMapper = recordMapper;
        this.riskResultMapper = riskResultMapper;
        this.interventionMapper = interventionMapper;
        this.participantMapper = participantMapper;
        this.dataScopeUtil = dataScopeUtil;
    }

    @Override
    public Map<String, Object> summary() {
        int scope = dataScopeUtil.getDataScope();
        Long currentUserId = StpUtil.getLoginIdAsLong();
        Long deptId = (scope == 1) ? dataScopeUtil.getCurrentDeptId() : null;

        long totalUsers;
        long highRiskCount;
        String examCompletionRate;

        if (scope == 2) {
            totalUsers = 1;
            highRiskCount = countHighRiskForUser(currentUserId);
            examCompletionRate = calcCompletionRateForUser(currentUserId);
        } else {
            totalUsers = countUsers(deptId);
            highRiskCount = countHighRisk(deptId);
            examCompletionRate = calcCompletionRateOverall(deptId);
        }

        long activeSchedules = scheduleMapper.selectCount(
                new LambdaQueryWrapper<ExamSchedule>().eq(ExamSchedule::getStatus, 1));
        long activeInterventions = interventionMapper.selectCount(
                new LambdaQueryWrapper<InterventionPlan>().eq(InterventionPlan::getStatus, 1));

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("totalUsers", totalUsers);
        data.put("activeSchedules", activeSchedules);
        data.put("highRiskCount", highRiskCount);
        data.put("activeInterventions", activeInterventions);
        data.put("examCompletionRate", examCompletionRate);
        return data;
    }

    @Override
    public Map<String, Object> riskDistribution() {
        int scope = dataScopeUtil.getDataScope();
        Long currentUserId = StpUtil.getLoginIdAsLong();

        List<HealthRiskResult> results;
        if (scope == 2) {
            results = riskResultMapper.selectList(
                    new LambdaQueryWrapper<HealthRiskResult>()
                            .eq(HealthRiskResult::getUserId, currentUserId));
        } else if (scope == 1) {
            List<Long> deptUserIds = getDeptUserIds(dataScopeUtil.getCurrentDeptId());
            results = deptUserIds.isEmpty() ? Collections.emptyList()
                    : riskResultMapper.selectList(new LambdaQueryWrapper<HealthRiskResult>()
                    .in(HealthRiskResult::getUserId, deptUserIds));
        } else {
            results = riskResultMapper.selectList(null);
        }

        long low = 0, mid = 0, high = 0;
        for (HealthRiskResult r : results) {
            switch (r.getRiskLevel()) {
                case "低": low++; break;
                case "中": mid++; break;
                case "高": high++; break;
            }
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("low", low);
        data.put("medium", mid);
        data.put("high", high);
        return data;
    }

    @Override
    public Map<String, Object> deptRanking() {
        List<Department> depts = deptMapper.selectList(null);

        List<User> allUsers = userMapper.selectList(
                new LambdaQueryWrapper<User>().isNotNull(User::getDeptId));
        Map<Long, List<User>> usersByDept = allUsers.stream()
                .collect(Collectors.groupingBy(User::getDeptId));

        List<HealthRiskResult> allResults = riskResultMapper.selectList(null);
        Map<Long, List<HealthRiskResult>> resultsByUser = allResults.stream()
                .collect(Collectors.groupingBy(HealthRiskResult::getUserId));

        List<Map<String, Object>> ranking = new ArrayList<>();
        for (Department dept : depts) {
            if (dept.getParentId() == 0) continue;

            List<User> deptUsers = usersByDept.getOrDefault(dept.getId(), Collections.emptyList());
            if (deptUsers.isEmpty()) {
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("deptName", dept.getName());
                item.put("avgScore", 0);
                item.put("userCount", 0);
                ranking.add(item);
                continue;
            }

            List<Long> deptUserIds = deptUsers.stream().map(User::getId).toList();
            double avgScore = deptUserIds.stream()
                    .flatMap(uid -> resultsByUser.getOrDefault(uid, Collections.emptyList()).stream())
                    .filter(r -> r.getTotalScore() != null)
                    .mapToDouble(r -> r.getTotalScore().doubleValue())
                    .average().orElse(0);

            Map<String, Object> item = new LinkedHashMap<>();
            item.put("deptName", dept.getName());
            item.put("avgScore", Math.round(avgScore * 10) / 10.0);
            item.put("userCount", deptUsers.size());
            ranking.add(item);
        }
        ranking.sort((a, b) -> Double.compare(
                ((Number) b.get("avgScore")).doubleValue(),
                ((Number) a.get("avgScore")).doubleValue()));

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("ranking", ranking);
        return data;
    }

    @Override
    public Map<String, Object> examTrend() {
        List<ExamSchedule> schedules = scheduleMapper.selectList(
                new LambdaQueryWrapper<ExamSchedule>().eq(ExamSchedule::getStatus, 2)
                        .orderByDesc(ExamSchedule::getStartDate).last("LIMIT 12"));

        List<Map<String, Object>> trend = new ArrayList<>();
        for (ExamSchedule s : schedules) {
            long examined = recordMapper.selectCount(new LambdaQueryWrapper<ExamRecord>()
                    .eq(ExamRecord::getScheduleId, s.getId()).eq(ExamRecord::getStatus, 1));
            long total = recordMapper.selectCount(new LambdaQueryWrapper<ExamRecord>()
                    .eq(ExamRecord::getScheduleId, s.getId()));
            Map<String, Object> point = new LinkedHashMap<>();
            point.put("title", s.getTitle());
            point.put("rate", total > 0 ? Math.round(examined * 100.0 / total) : 0);
            point.put("examined", examined);
            point.put("total", total);
            trend.add(point);
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("trend", trend);
        return data;
    }

    @Override
    public Map<String, Object> interventionStats() {
        List<InterventionPlan> plans = interventionMapper.selectList(null);

        List<Map<String, Object>> stats = new ArrayList<>();
        for (InterventionPlan plan : plans) {
            long total = participantMapper.selectCount(
                    new LambdaQueryWrapper<InterventionParticipant>()
                            .eq(InterventionParticipant::getPlanId, plan.getId()));
            long completed = participantMapper.selectCount(
                    new LambdaQueryWrapper<InterventionParticipant>()
                            .eq(InterventionParticipant::getPlanId, plan.getId())
                            .eq(InterventionParticipant::getStatus, 2));
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("planTitle", plan.getTitle());
            item.put("type", plan.getType());
            item.put("totalParticipants", total);
            item.put("completedParticipants", completed);
            item.put("completionRate", total > 0 ? Math.round(completed * 100.0 / total) + "%" : "0%");
            stats.add(item);
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("stats", stats);
        return data;
    }

    private long countUsers(Long deptId) {
        LambdaQueryWrapper<User> qw = new LambdaQueryWrapper<User>().eq(User::getStatus, 1);
        if (deptId != null) qw.eq(User::getDeptId, deptId);
        return userMapper.selectCount(qw);
    }

    private long countHighRisk(Long deptId) {
        if (deptId != null) {
            List<Long> deptUserIds = getDeptUserIds(deptId);
            return deptUserIds.isEmpty() ? 0
                    : riskResultMapper.selectCount(new LambdaQueryWrapper<HealthRiskResult>()
                    .in(HealthRiskResult::getUserId, deptUserIds)
                    .eq(HealthRiskResult::getRiskLevel, "高"));
        }
        return riskResultMapper.selectCount(
                new LambdaQueryWrapper<HealthRiskResult>().eq(HealthRiskResult::getRiskLevel, "高"));
    }

    private long countHighRiskForUser(Long userId) {
        return riskResultMapper.selectCount(new LambdaQueryWrapper<HealthRiskResult>()
                .eq(HealthRiskResult::getUserId, userId)
                .eq(HealthRiskResult::getRiskLevel, "高"));
    }

    private String calcCompletionRateForUser(Long userId) {
        long examined = recordMapper.selectCount(new LambdaQueryWrapper<ExamRecord>()
                .eq(ExamRecord::getUserId, userId).eq(ExamRecord::getStatus, 1));
        long total = recordMapper.selectCount(new LambdaQueryWrapper<ExamRecord>()
                .eq(ExamRecord::getUserId, userId));
        return total > 0 ? Math.round(examined * 100.0 / total) + "%" : "0%";
    }

    private String calcCompletionRateOverall(Long deptId) {
        if (deptId != null) {
            List<Long> deptUserIds = getDeptUserIds(deptId);
            if (deptUserIds.isEmpty()) return "0%";
            long examined = recordMapper.selectCount(new LambdaQueryWrapper<ExamRecord>()
                    .in(ExamRecord::getUserId, deptUserIds).eq(ExamRecord::getStatus, 1));
            long total = recordMapper.selectCount(new LambdaQueryWrapper<ExamRecord>()
                    .in(ExamRecord::getUserId, deptUserIds));
            return total > 0 ? Math.round(examined * 100.0 / total) + "%" : "0%";
        }
        long examined = recordMapper.selectCount(
                new LambdaQueryWrapper<ExamRecord>().eq(ExamRecord::getStatus, 1));
        long total = recordMapper.selectCount(new LambdaQueryWrapper<ExamRecord>());
        return total > 0 ? Math.round(examined * 100.0 / total) + "%" : "0%";
    }

    private List<Long> getDeptUserIds(Long deptId) {
        return userMapper.selectList(
                new LambdaQueryWrapper<User>().eq(User::getDeptId, deptId))
                .stream().map(User::getId).collect(Collectors.toList());
    }
}
