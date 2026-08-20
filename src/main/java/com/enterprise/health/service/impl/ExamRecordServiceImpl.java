package com.enterprise.health.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.enterprise.health.common.exception.BusinessException;
import com.enterprise.health.common.util.DataScopeUtil;
import com.enterprise.health.domain.ExamResultDTO;
import com.enterprise.health.entity.ExamPackageItem;
import com.enterprise.health.entity.ExamRecord;
import com.enterprise.health.entity.ExamResultItem;
import com.enterprise.health.entity.ExamSchedule;
import com.enterprise.health.entity.User;
import com.enterprise.health.mapper.ExamPackageItemMapper;
import com.enterprise.health.mapper.ExamRecordMapper;
import com.enterprise.health.mapper.ExamResultItemMapper;
import com.enterprise.health.mapper.ExamScheduleMapper;
import com.enterprise.health.mapper.UserMapper;
import com.enterprise.health.service.ExamRecordService;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Comparator;
import java.util.stream.Collectors;

@Service
public class ExamRecordServiceImpl implements ExamRecordService {

    private final ExamRecordMapper recordMapper;
    private final ExamResultItemMapper resultItemMapper;
    private final ExamPackageItemMapper packageItemMapper;
    private final ExamScheduleMapper scheduleMapper;
    private final UserMapper userMapper;
    private final DataScopeUtil dataScopeUtil;

    public ExamRecordServiceImpl(ExamRecordMapper recordMapper, ExamResultItemMapper resultItemMapper,
                                  ExamPackageItemMapper packageItemMapper, ExamScheduleMapper scheduleMapper,
                                  UserMapper userMapper, DataScopeUtil dataScopeUtil) {
        this.recordMapper = recordMapper;
        this.resultItemMapper = resultItemMapper;
        this.packageItemMapper = packageItemMapper;
        this.scheduleMapper = scheduleMapper;
        this.userMapper = userMapper;
        this.dataScopeUtil = dataScopeUtil;
    }

    @Override
    public Map<String, Object> list(int page, int size, Long scheduleId, Long deptId, Integer status) {
        LambdaQueryWrapper<ExamRecord> qw = new LambdaQueryWrapper<>();
        if (scheduleId != null) qw.eq(ExamRecord::getScheduleId, scheduleId);
        if (status != null) qw.eq(ExamRecord::getStatus, status);

        int scope = dataScopeUtil.getDataScope();
        if (scope == 1) {
            List<Long> deptUserIds = userMapper.selectList(
                    new LambdaQueryWrapper<User>().eq(User::getDeptId, dataScopeUtil.getCurrentDeptId()))
                    .stream().map(User::getId).collect(Collectors.toList());
            if (deptUserIds.isEmpty()) {
                Map<String, Object> empty = new HashMap<>();
                empty.put("records", Collections.emptyList());
                empty.put("total", 0L);
                empty.put("page", (long) page);
                empty.put("size", (long) size);
                return empty;
            }
            qw.in(ExamRecord::getUserId, deptUserIds);
        } else if (scope == 2) {
            qw.eq(ExamRecord::getUserId, StpUtil.getLoginIdAsLong());
        } else if (deptId != null) {
            List<Long> deptUserIds = userMapper.selectList(
                    new LambdaQueryWrapper<User>().eq(User::getDeptId, deptId))
                    .stream().map(User::getId).collect(Collectors.toList());
            if (!deptUserIds.isEmpty()) qw.in(ExamRecord::getUserId, deptUserIds);
        }

        qw.orderByDesc(ExamRecord::getCreatedAt);
        Page<ExamRecord> result = recordMapper.selectPage(new Page<>(page, size), qw);

        Map<String, Object> data = new HashMap<>();
        data.put("records", result.getRecords());
        data.put("total", result.getTotal());
        data.put("page", result.getCurrent());
        data.put("size", result.getSize());
        return data;
    }

    @Override
    public Map<String, Object> detail(Long recordId) {
        ExamRecord record = recordMapper.selectById(recordId);
        if (record == null) throw new BusinessException(400, "记录不存在");
        checkViewPermission(record);
        ExamSchedule schedule = scheduleMapper.selectById(record.getScheduleId());
        User user = userMapper.selectById(record.getUserId());
        List<ExamResultItem> items = resultItemMapper.selectList(
                new LambdaQueryWrapper<ExamResultItem>().eq(ExamResultItem::getRecordId, recordId)
                        .orderByAsc(ExamResultItem::getSortOrder));

        List<ExamPackageItem> pkgItems = null;
        if (schedule != null) {
            pkgItems = packageItemMapper.selectList(
                    new LambdaQueryWrapper<ExamPackageItem>().eq(ExamPackageItem::getPackageId, schedule.getPackageId()));
        }

        Map<String, Object> result = new HashMap<>();
        result.put("id", record.getId().toString());
        result.put("userId", record.getUserId().toString());
        result.put("userName", user != null ? user.getRealName() : "");
        result.put("employeeNo", user != null ? user.getEmployeeNo() : "");
        result.put("deptId", user != null && user.getDeptId() != null ? user.getDeptId().toString() : null);
        result.put("scheduleTitle", schedule != null ? schedule.getTitle() : "");
        result.put("examDate", record.getExamDate() != null ? record.getExamDate().toString() : null);
        result.put("status", record.getStatus());
        result.put("overallResult", record.getOverallResult());

        List<Map<String, Object>> itemDetails = new ArrayList<>();
        Map<Long, ExamResultItem> itemMap = new HashMap<>();
        if (items != null) {
            for (ExamResultItem item : items) itemMap.put(item.getPackageItemId(), item);
        }

        if (pkgItems != null) {
            for (ExamPackageItem pi : pkgItems) {
                Map<String, Object> detail = new HashMap<>();
                detail.put("packageItemId", pi.getId().toString());
                detail.put("itemName", pi.getItemName());
                detail.put("category", pi.getItemCategory());
                detail.put("unit", pi.getUnit());
                detail.put("refMin", pi.getRefMin());
                detail.put("refMax", pi.getRefMax());
                ExamResultItem ri = itemMap.get(pi.getId());
                detail.put("value", ri != null ? ri.getItemValue() : null);
                detail.put("isAbnormal", ri != null ? ri.getIsAbnormal() : null);
                itemDetails.add(detail);
            }
        }
        result.put("items", itemDetails);
        return result;
    }

    @Override
    @Transactional
    public void enterResult(ExamResultDTO dto) {
        ExamRecord record = recordMapper.selectById(dto.recordId());
        if (record == null) throw new BusinessException(400, "体检记录不存在");

        resultItemMapper.delete(new LambdaQueryWrapper<ExamResultItem>()
                .eq(ExamResultItem::getRecordId, dto.recordId()));

        int sort = 1;
        for (ExamResultDTO.ItemValue iv : dto.items()) {
            ExamPackageItem pi = packageItemMapper.selectById(iv.packageItemId());
            if (pi == null) continue;

            ExamResultItem ri = new ExamResultItem();
            ri.setRecordId(dto.recordId());
            ri.setPackageItemId(iv.packageItemId());
            ri.setItemValue(iv.itemValue());
            ri.setIsAbnormal(calcAbnormal(pi, iv.itemValue()));
            ri.setSortOrder(sort++);
            resultItemMapper.insert(ri);
        }

        if (dto.examDate() != null && !dto.examDate().isEmpty())
            record.setExamDate(LocalDate.parse(dto.examDate()));
        if (dto.overallResult() != null) record.setOverallResult(dto.overallResult());
        record.setStatus(1);
        record.setUpdatedBy(StpUtil.getLoginIdAsLong());
        recordMapper.updateById(record);
    }

    @Override
    public Map<String, Object> mine(Long userId) {
        List<ExamRecord> records = recordMapper.selectList(
                new LambdaQueryWrapper<ExamRecord>().eq(ExamRecord::getUserId, userId)
                        .orderByDesc(ExamRecord::getCreatedAt));

        List<Map<String, Object>> list = new ArrayList<>();
        for (ExamRecord r : records) {
            ExamSchedule s = scheduleMapper.selectById(r.getScheduleId());
            Map<String, Object> m = new HashMap<>();
            m.put("id", r.getId().toString());
            m.put("scheduleTitle", s != null ? s.getTitle() : "");
            m.put("examDate", r.getExamDate() != null ? r.getExamDate().toString() : null);
            m.put("status", r.getStatus());
            m.put("overallResult", r.getOverallResult());
            list.add(m);
        }
        Map<String, Object> result = new HashMap<>();
        result.put("records", list);
        result.put("total", list.size());
        return result;
    }

    // 校验当前用户是否有权查看这条体检记录：scope=0 全量，scope=1 本部门，scope=2 仅本人
    private void checkViewPermission(ExamRecord record) {
        int scope = dataScopeUtil.getDataScope();
        if (scope == 0) return;
        if (scope == 2) {
            if (!record.getUserId().equals(StpUtil.getLoginIdAsLong())) {
                throw new BusinessException(403, "无权查看他人体检记录");
            }
            return;
        }
        // scope == 1：本部门
        User owner = userMapper.selectById(record.getUserId());
        Long deptId = dataScopeUtil.getCurrentDeptId();
        if (owner == null || owner.getDeptId() == null || !owner.getDeptId().equals(deptId)) {
            throw new BusinessException(403, "无权查看其他部门的体检记录");
        }
    }

    private int calcAbnormal(ExamPackageItem pi, String value) {
        if (value == null || value.isBlank()) return 0;
        try {
            BigDecimal v = new BigDecimal(value);
            if (pi.getRefMax() != null && v.compareTo(pi.getRefMax()) > 0) return 1;
            if (pi.getRefMin() != null && v.compareTo(pi.getRefMin()) < 0) return 2;
            return 0;
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
