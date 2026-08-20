package com.enterprise.health.service;

import com.enterprise.health.domain.ExamResultDTO;
import java.util.Map;

public interface ExamRecordService {
    Map<String, Object> list(int page, int size, Long scheduleId, Long deptId, Integer status);
    Map<String, Object> detail(Long recordId);
    void enterResult(ExamResultDTO dto);
    Map<String, Object> mine(Long userId);
}
