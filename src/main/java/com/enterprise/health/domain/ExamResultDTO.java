package com.enterprise.health.domain;

import java.util.List;

public record ExamResultDTO(Long recordId, String examDate, String overallResult, List<ItemValue> items) {

    public record ItemValue(Long packageItemId, String itemValue) {}
}
