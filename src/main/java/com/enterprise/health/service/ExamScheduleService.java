package com.enterprise.health.service;

import com.enterprise.health.domain.ScheduleDTO;
import com.enterprise.health.entity.ExamSchedule;
import java.util.Map;

public interface ExamScheduleService {
    Map<String, Object> list(int page, int size);
    ExamSchedule create(ScheduleDTO dto);
    ExamSchedule update(Long id, ScheduleDTO dto);
    void changeStatus(Long id, Integer status);
}
