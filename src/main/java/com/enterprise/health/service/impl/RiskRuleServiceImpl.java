package com.enterprise.health.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.enterprise.health.common.exception.BusinessException;
import com.enterprise.health.entity.ExamPackageItem;
import com.enterprise.health.entity.HealthRiskRule;
import com.enterprise.health.mapper.ExamPackageItemMapper;
import com.enterprise.health.mapper.HealthRiskRuleMapper;
import com.enterprise.health.service.RiskRuleService;
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
public class RiskRuleServiceImpl implements RiskRuleService {

    private final HealthRiskRuleMapper ruleMapper;
    private final ExamPackageItemMapper itemMapper;

    public RiskRuleServiceImpl(HealthRiskRuleMapper ruleMapper, ExamPackageItemMapper itemMapper) {
        this.ruleMapper = ruleMapper;
        this.itemMapper = itemMapper;
    }

    @Override
    public List<Map<String, Object>> list() {
        List<HealthRiskRule> rules = ruleMapper.selectList(
                new LambdaQueryWrapper<HealthRiskRule>().orderByAsc(HealthRiskRule::getRiskLevel));
        List<Map<String, Object>> result = new ArrayList<>();
        for (HealthRiskRule rule : rules) {
            Map<String, Object> map = toMap(rule);
            ExamPackageItem item = itemMapper.selectById(rule.getPackageItemId());
            if (item != null) {
                map.put("itemName", item.getItemName());
                map.put("itemCategory", item.getItemCategory());
                map.put("unit", item.getUnit());
                map.put("refMin", item.getRefMin());
                map.put("refMax", item.getRefMax());
            }
            result.add(map);
        }
        return result;
    }

    @Override
    @Transactional
    public HealthRiskRule create(HealthRiskRule rule) {
        ruleMapper.insert(rule);
        return rule;
    }

    @Override
    public HealthRiskRule update(Long id, HealthRiskRule update) {
        HealthRiskRule existing = ruleMapper.selectById(id);
        if (existing == null) throw new BusinessException(400, "规则不存在");
        if (update.getRuleName() != null) existing.setRuleName(update.getRuleName());
        if (update.getPackageItemId() != null) existing.setPackageItemId(update.getPackageItemId());
        if (update.getRiskLevel() != null) existing.setRiskLevel(update.getRiskLevel());
        if (update.getConditionType() != null) existing.setConditionType(update.getConditionType());
        if (update.getThresholdValue() != null) existing.setThresholdValue(update.getThresholdValue());
        if (update.getScore() != null) existing.setScore(update.getScore());
        if (update.getWeight() != null) existing.setWeight(update.getWeight());
        if (update.getStatus() != null) existing.setStatus(update.getStatus());
        ruleMapper.updateById(existing);
        return existing;
    }

    @Override
    public void delete(Long id) {
        ruleMapper.deleteById(id);
    }

    private Map<String, Object> toMap(HealthRiskRule rule) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", rule.getId().toString());
        map.put("packageItemId", rule.getPackageItemId() != null ? rule.getPackageItemId().toString() : null);
        map.put("ruleName", rule.getRuleName());
        map.put("riskLevel", rule.getRiskLevel());
        map.put("conditionType", rule.getConditionType());
        map.put("thresholdValue", rule.getThresholdValue());
        map.put("score", rule.getScore());
        map.put("weight", rule.getWeight());
        map.put("status", rule.getStatus());
        return map;
    }
}
