package com.enterprise.health.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.enterprise.health.common.exception.BusinessException;
import com.enterprise.health.common.util.DataScopeUtil;
import com.enterprise.health.entity.FollowUpRecord;
import com.enterprise.health.entity.InterventionPlan;
import com.enterprise.health.entity.User;
import com.enterprise.health.mapper.FollowUpRecordMapper;
import com.enterprise.health.mapper.InterventionPlanMapper;
import com.enterprise.health.mapper.UserMapper;
import com.enterprise.health.service.FollowUpService;
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
public class FollowUpServiceImpl implements FollowUpService {

    private final FollowUpRecordMapper followUpMapper;
    private final UserMapper userMapper;
    private final InterventionPlanMapper planMapper;
    private final DataScopeUtil dataScopeUtil;

    public FollowUpServiceImpl(FollowUpRecordMapper followUpMapper, UserMapper userMapper,
                                InterventionPlanMapper planMapper, DataScopeUtil dataScopeUtil) {
        this.followUpMapper = followUpMapper;
        this.userMapper = userMapper;
        this.planMapper = planMapper;
        this.dataScopeUtil = dataScopeUtil;
    }

    @Override
    public Map<String, Object> list(int page, int size, Long planId, Long userId) {
        LambdaQueryWrapper<FollowUpRecord> qw = new LambdaQueryWrapper<>();

        int scope = dataScopeUtil.getDataScope();
        if (scope == 2) {
            qw.eq(FollowUpRecord::getUserId, StpUtil.getLoginIdAsLong());
        } else if (scope == 1) {
            List<Long> deptUserIds = getDeptUserIds(dataScopeUtil.getCurrentDeptId());
            if (deptUserIds.isEmpty()) return emptyResult(page, size);
            qw.in(FollowUpRecord::getUserId, deptUserIds);
        }

        if (planId != null) qw.eq(FollowUpRecord::getPlanId, planId);
        if (userId != null) qw.eq(FollowUpRecord::getUserId, userId);
        qw.orderByDesc(FollowUpRecord::getFollowDate);

        Page<FollowUpRecord> result = followUpMapper.selectPage(new Page<>(page, size), qw);

        List<Map<String, Object>> records = new ArrayList<>();
        for (FollowUpRecord r : result.getRecords()) {
            Map<String, Object> map = toMap(r);
            User u = userMapper.selectById(r.getUserId());
            map.put("userName", u != null ? u.getRealName() : "");
            map.put("employeeNo", u != null ? u.getEmployeeNo() : "");
            InterventionPlan plan = planMapper.selectById(r.getPlanId());
            map.put("planTitle", plan != null ? plan.getTitle() : "");
            User recorder = userMapper.selectById(r.getRecordedBy());
            map.put("recordedByName", recorder != null ? recorder.getRealName() : "");
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
    public FollowUpRecord create(FollowUpRecord record) {
        if (record.getPlanId() == null || record.getUserId() == null || record.getFollowDate() == null)
            throw new BusinessException(400, "计划、用户、随访日期不能为空");
        record.setRecordedBy(StpUtil.getLoginIdAsLong());
        followUpMapper.insert(record);
        return record;
    }

    @Override
    public FollowUpRecord update(Long id, FollowUpRecord update) {
        FollowUpRecord existing = followUpMapper.selectById(id);
        if (existing == null) throw new BusinessException(400, "随访记录不存在");
        if (update.getContent() != null) existing.setContent(update.getContent());
        if (update.getResult() != null) existing.setResult(update.getResult());
        if (update.getNextDate() != null) existing.setNextDate(update.getNextDate());
        followUpMapper.updateById(existing);
        return existing;
    }

    private List<Long> getDeptUserIds(Long deptId) {
        return userMapper.selectList(
                new LambdaQueryWrapper<User>().eq(User::getDeptId, deptId))
                .stream().map(User::getId).toList();
    }

    private Map<String, Object> toMap(FollowUpRecord r) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", r.getId().toString());
        map.put("planId", r.getPlanId() != null ? r.getPlanId().toString() : null);
        map.put("userId", r.getUserId() != null ? r.getUserId().toString() : null);
        map.put("followDate", r.getFollowDate() != null ? r.getFollowDate().toString() : null);
        map.put("content", r.getContent());
        map.put("result", r.getResult());
        map.put("nextDate", r.getNextDate() != null ? r.getNextDate().toString() : null);
        map.put("recordedBy", r.getRecordedBy() != null ? r.getRecordedBy().toString() : null);
        map.put("createdAt", r.getCreatedAt() != null ? r.getCreatedAt().toString() : null);
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
