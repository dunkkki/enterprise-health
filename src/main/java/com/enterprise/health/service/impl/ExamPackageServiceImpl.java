package com.enterprise.health.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.enterprise.health.common.exception.BusinessException;
import com.enterprise.health.entity.ExamPackage;
import com.enterprise.health.entity.ExamPackageItem;
import com.enterprise.health.mapper.ExamPackageMapper;
import com.enterprise.health.mapper.ExamPackageItemMapper;
import com.enterprise.health.service.ExamPackageService;
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
public class ExamPackageServiceImpl implements ExamPackageService {

    private final ExamPackageMapper packageMapper;
    private final ExamPackageItemMapper itemMapper;

    public ExamPackageServiceImpl(ExamPackageMapper packageMapper, ExamPackageItemMapper itemMapper) {
        this.packageMapper = packageMapper;
        this.itemMapper = itemMapper;
    }

    @Override
    public List<ExamPackage> list() {
        return packageMapper.selectList(new LambdaQueryWrapper<ExamPackage>()
                .eq(ExamPackage::getStatus, 1));
    }

    @Override
    public Map<String, Object> detail(Long id) {
        ExamPackage pkg = packageMapper.selectById(id);
        if (pkg == null) throw new BusinessException(400, "套餐不存在");
        List<ExamPackageItem> items = itemMapper.selectList(
                new LambdaQueryWrapper<ExamPackageItem>()
                        .eq(ExamPackageItem::getPackageId, id)
                        .orderByAsc(ExamPackageItem::getSortOrder));
        Map<String, Object> result = new HashMap<>();
        result.put("id", pkg.getId().toString());
        result.put("name", pkg.getName());
        result.put("description", pkg.getDescription());
        result.put("applicableGender", pkg.getApplicableGender());
        result.put("price", pkg.getPrice());
        result.put("status", pkg.getStatus());
        result.put("items", items);
        return result;
    }

    @Override
    @Transactional
    public ExamPackage create(ExamPackage pkg) {
        pkg.setStatus(1);
        packageMapper.insert(pkg);
        return pkg;
    }

    @Override
    public ExamPackage update(Long id, ExamPackage update) {
        ExamPackage existing = packageMapper.selectById(id);
        if (existing == null) throw new BusinessException(400, "套餐不存在");
        if (update.getName() != null) existing.setName(update.getName());
        if (update.getDescription() != null) existing.setDescription(update.getDescription());
        if (update.getApplicableGender() != null) existing.setApplicableGender(update.getApplicableGender());
        if (update.getPrice() != null) existing.setPrice(update.getPrice());
        packageMapper.updateById(existing);
        return existing;
    }

    @Override
    @Transactional
    public void delete(Long id) {
        packageMapper.deleteById(id);
        itemMapper.delete(new LambdaQueryWrapper<ExamPackageItem>().eq(ExamPackageItem::getPackageId, id));
    }
}
