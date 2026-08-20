package com.enterprise.health.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.enterprise.health.common.exception.BusinessException;

import com.enterprise.health.domain.ScheduleDTO;
import com.enterprise.health.entity.ExamPackage;
import com.enterprise.health.entity.ExamRecord;
import com.enterprise.health.entity.ExamSchedule;
import com.enterprise.health.entity.ScheduleDept;
import com.enterprise.health.entity.User;
import com.enterprise.health.mapper.ExamPackageMapper;
import com.enterprise.health.mapper.ExamRecordMapper;
import com.enterprise.health.mapper.ExamScheduleMapper;
import com.enterprise.health.mapper.ScheduleDeptMapper;
import com.enterprise.health.mapper.UserMapper;
import com.enterprise.health.service.ExamScheduleService;
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
public class ExamScheduleServiceImpl implements ExamScheduleService {

    private final ExamScheduleMapper scheduleMapper;
    private final ExamPackageMapper packageMapper;
    private final ExamRecordMapper recordMapper;
    private final UserMapper userMapper;
    private final ScheduleDeptMapper scheduleDeptMapper;

    public ExamScheduleServiceImpl(ExamScheduleMapper scheduleMapper, ExamPackageMapper packageMapper,
                                   ExamRecordMapper recordMapper, UserMapper userMapper,
                                   ScheduleDeptMapper scheduleDeptMapper) {
        this.scheduleMapper = scheduleMapper;
        this.packageMapper = packageMapper;
        this.recordMapper = recordMapper;
        this.userMapper = userMapper;
        this.scheduleDeptMapper = scheduleDeptMapper;
    }

    @Override
    public Map<String, Object> list(int page, int size) {
        LambdaQueryWrapper<ExamSchedule> qw = new LambdaQueryWrapper<ExamSchedule>()
                .orderByDesc(ExamSchedule::getCreatedAt);

        Page<ExamSchedule> result = scheduleMapper.selectPage(new Page<>(page, size), qw);

        List<Map<String, Object>> records = new ArrayList<>();
        for (ExamSchedule s : result.getRecords()) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", s.getId().toString());
            map.put("title", s.getTitle());
            map.put("packageId", s.getPackageId() != null ? s.getPackageId().toString() : null);
            map.put("startDate", s.getStartDate() != null ? s.getStartDate().toString() : null);
            map.put("endDate", s.getEndDate() != null ? s.getEndDate().toString() : null);
            map.put("status", s.getStatus());
            map.put("createdBy", s.getCreatedBy() != null ? s.getCreatedBy().toString() : null);
            map.put("createdAt", s.getCreatedAt() != null ? s.getCreatedAt().toString() : null);

            List<Long> deptIds = scheduleDeptMapper.selectList(
                    new LambdaQueryWrapper<ScheduleDept>()
                            .eq(ScheduleDept::getScheduleId, s.getId()))
                    .stream().map(ScheduleDept::getDeptId).toList();
            map.put("targetDeptIds", deptIds);

            ExamPackage pkg = packageMapper.selectById(s.getPackageId());
            map.put("packageName", pkg != null ? pkg.getName() : "");

            long examined = recordMapper.selectCount(new LambdaQueryWrapper<ExamRecord>()
                    .eq(ExamRecord::getScheduleId, s.getId()).eq(ExamRecord::getStatus, 1));
            long total = recordMapper.selectCount(new LambdaQueryWrapper<ExamRecord>()
                    .eq(ExamRecord::getScheduleId, s.getId()));
            map.put("examinedCount", examined);
            map.put("totalCount", total);

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
    public ExamSchedule create(ScheduleDTO dto) {
        if (packageMapper.selectById(dto.packageId()) == null)
            throw new BusinessException(400, "体检套餐不存在");

        ExamSchedule schedule = new ExamSchedule();
        schedule.setTitle(dto.title());
        schedule.setPackageId(dto.packageId());
        schedule.setStartDate(dto.startDate());
        schedule.setEndDate(dto.endDate());
        schedule.setStatus(0);
        schedule.setCreatedBy(StpUtil.getLoginIdAsLong());
        scheduleMapper.insert(schedule);

        if (dto.targetDeptIds() != null) {
            for (Long deptId : dto.targetDeptIds()) {
                ScheduleDept sd = new ScheduleDept();
                sd.setScheduleId(schedule.getId());
                sd.setDeptId(deptId);
                scheduleDeptMapper.insert(sd);
            }
        }

        createRecordsForSchedule(schedule, dto.targetDeptIds());
        return schedule;
    }

    @Override
    @Transactional
    public ExamSchedule update(Long id, ScheduleDTO dto) {
        ExamSchedule existing = scheduleMapper.selectById(id);
        if (existing == null) throw new BusinessException(400, "排期不存在");

        if (dto.title() != null) existing.setTitle(dto.title());
        if (dto.startDate() != null) existing.setStartDate(dto.startDate());
        if (dto.endDate() != null) existing.setEndDate(dto.endDate());
        scheduleMapper.updateById(existing);

        if (dto.targetDeptIds() != null) {
            scheduleDeptMapper.delete(new LambdaQueryWrapper<ScheduleDept>()
                    .eq(ScheduleDept::getScheduleId, id));
            for (Long deptId : dto.targetDeptIds()) {
                ScheduleDept sd = new ScheduleDept();
                sd.setScheduleId(id);
                sd.setDeptId(deptId);
                scheduleDeptMapper.insert(sd);
            }
        }

        return existing;
    }

    @Override
    @Transactional
    public void changeStatus(Long id, Integer status) {
        ExamSchedule schedule = scheduleMapper.selectById(id);
        if (schedule == null) throw new BusinessException(400, "排期不存在");
        schedule.setStatus(status);
        scheduleMapper.updateById(schedule);
        if (status == 1) {
            List<Long> deptIds = scheduleDeptMapper.selectList(
                    new LambdaQueryWrapper<ScheduleDept>()
                            .eq(ScheduleDept::getScheduleId, id))
                    .stream().map(ScheduleDept::getDeptId).toList();
            createRecordsForSchedule(schedule, deptIds);
        }
    }

    private void createRecordsForSchedule(ExamSchedule schedule, List<Long> deptIds) {
        LambdaQueryWrapper<User> qw = new LambdaQueryWrapper<User>().eq(User::getStatus, 1);
        if (deptIds != null && !deptIds.isEmpty()) {
            qw.in(User::getDeptId, deptIds);
        }
        List<User> users = userMapper.selectList(qw);
        for (User u : users) {
            if (recordMapper.selectCount(new LambdaQueryWrapper<ExamRecord>()
                    .eq(ExamRecord::getUserId, u.getId())
                    .eq(ExamRecord::getScheduleId, schedule.getId())) == 0) {
                ExamRecord record = new ExamRecord();
                record.setUserId(u.getId());
                record.setScheduleId(schedule.getId());
                record.setStatus(0);
                recordMapper.insert(record);
            }
        }
    }
}
