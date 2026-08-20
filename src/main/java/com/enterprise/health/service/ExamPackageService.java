package com.enterprise.health.service;

import com.enterprise.health.entity.ExamPackage;
import java.util.List;
import java.util.Map;

public interface ExamPackageService {
    List<ExamPackage> list();
    Map<String, Object> detail(Long id);
    ExamPackage create(ExamPackage pkg);
    ExamPackage update(Long id, ExamPackage pkg);
    void delete(Long id);
}
