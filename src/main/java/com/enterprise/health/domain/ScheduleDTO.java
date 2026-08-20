package com.enterprise.health.domain;

import java.time.LocalDate;
import java.util.List;

public record ScheduleDTO(String title, Long packageId, LocalDate startDate,
                           LocalDate endDate, List<Long> targetDeptIds, Integer status) {}
