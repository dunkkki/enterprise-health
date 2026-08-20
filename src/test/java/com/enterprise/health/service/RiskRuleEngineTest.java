package com.enterprise.health.service;

import com.enterprise.health.entity.ExamPackageItem;
import com.enterprise.health.entity.ExamResultItem;
import com.enterprise.health.entity.HealthRiskRule;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class RiskRuleEngineTest {

    // Replicated from RiskResultServiceImpl.evaluateCondition for pure unit testing
    private boolean evaluateCondition(HealthRiskRule rule, ExamResultItem item, ExamPackageItem pi) {
        String value = item.getItemValue();
        if (value == null || value.isBlank()) return false;

        switch (rule.getConditionType()) {
            case "gt": {
                BigDecimal v = parseNumber(value);
                BigDecimal t = parseNumber(rule.getThresholdValue());
                return v != null && t != null && v.compareTo(t) > 0;
            }
            case "lt": {
                BigDecimal v = parseNumber(value);
                BigDecimal t = parseNumber(rule.getThresholdValue());
                return v != null && t != null && v.compareTo(t) < 0;
            }
            case "out_of_range": {
                BigDecimal v = parseNumber(value);
                if (v == null) return false;
                if (pi.getRefMin() != null && v.compareTo(pi.getRefMin()) < 0) return true;
                if (pi.getRefMax() != null && v.compareTo(pi.getRefMax()) > 0) return true;
                return false;
            }
            case "equals":
                return value.equals(rule.getThresholdValue());
            default:
                return false;
        }
    }

    private BigDecimal parseNumber(String s) {
        try { return new BigDecimal(s); }
        catch (NumberFormatException e) { return null; }
    }

    // --- Tests ---

    @Test
    void gt_shouldTriggerWhenAboveThreshold() {
        HealthRiskRule rule = new HealthRiskRule();
        rule.setConditionType("gt");
        rule.setThresholdValue("140");

        ExamResultItem item = new ExamResultItem();
        item.setItemValue("150");

        ExamPackageItem pi = new ExamPackageItem();

        assertTrue(evaluateCondition(rule, item, pi));
    }

    @Test
    void gt_shouldNotTriggerWhenBelowThreshold() {
        HealthRiskRule rule = new HealthRiskRule();
        rule.setConditionType("gt");
        rule.setThresholdValue("140");

        ExamResultItem item = new ExamResultItem();
        item.setItemValue("120");

        ExamPackageItem pi = new ExamPackageItem();

        assertFalse(evaluateCondition(rule, item, pi));
    }

    @Test
    void lt_shouldTriggerWhenBelowThreshold() {
        HealthRiskRule rule = new HealthRiskRule();
        rule.setConditionType("lt");
        rule.setThresholdValue("1.0");

        ExamResultItem item = new ExamResultItem();
        item.setItemValue("0.9");

        ExamPackageItem pi = new ExamPackageItem();

        assertTrue(evaluateCondition(rule, item, pi));
    }

    @Test
    void outOfRange_shouldTriggerWhenAboveMax() {
        HealthRiskRule rule = new HealthRiskRule();
        rule.setConditionType("out_of_range");

        ExamResultItem item = new ExamResultItem();
        item.setItemValue("150");

        ExamPackageItem pi = new ExamPackageItem();
        pi.setRefMin(new BigDecimal("90"));
        pi.setRefMax(new BigDecimal("140"));

        assertTrue(evaluateCondition(rule, item, pi));
    }

    @Test
    void outOfRange_shouldTriggerWhenBelowMin() {
        HealthRiskRule rule = new HealthRiskRule();
        rule.setConditionType("out_of_range");

        ExamResultItem item = new ExamResultItem();
        item.setItemValue("80");

        ExamPackageItem pi = new ExamPackageItem();
        pi.setRefMin(new BigDecimal("90"));
        pi.setRefMax(new BigDecimal("140"));

        assertTrue(evaluateCondition(rule, item, pi));
    }

    @Test
    void outOfRange_shouldNotTriggerWhenInRange() {
        HealthRiskRule rule = new HealthRiskRule();
        rule.setConditionType("out_of_range");

        ExamResultItem item = new ExamResultItem();
        item.setItemValue("120");

        ExamPackageItem pi = new ExamPackageItem();
        pi.setRefMin(new BigDecimal("90"));
        pi.setRefMax(new BigDecimal("140"));

        assertFalse(evaluateCondition(rule, item, pi));
    }

    @Test
    void equals_shouldTriggerWhenMatch() {
        HealthRiskRule rule = new HealthRiskRule();
        rule.setConditionType("equals");
        rule.setThresholdValue("阳性");

        ExamResultItem item = new ExamResultItem();
        item.setItemValue("阳性");

        ExamPackageItem pi = new ExamPackageItem();

        assertTrue(evaluateCondition(rule, item, pi));
    }

    @Test
    void equals_shouldNotTriggerWhenMismatch() {
        HealthRiskRule rule = new HealthRiskRule();
        rule.setConditionType("equals");
        rule.setThresholdValue("阳性");

        ExamResultItem item = new ExamResultItem();
        item.setItemValue("阴性");

        ExamPackageItem pi = new ExamPackageItem();

        assertFalse(evaluateCondition(rule, item, pi));
    }

    @Test
    void nullValue_shouldReturnFalse() {
        HealthRiskRule rule = new HealthRiskRule();
        rule.setConditionType("gt");
        rule.setThresholdValue("140");

        ExamResultItem item = new ExamResultItem();
        item.setItemValue(null);

        ExamPackageItem pi = new ExamPackageItem();

        assertFalse(evaluateCondition(rule, item, pi));
    }

    @Test
    void blankValue_shouldReturnFalse() {
        HealthRiskRule rule = new HealthRiskRule();
        rule.setConditionType("gt");
        rule.setThresholdValue("140");

        ExamResultItem item = new ExamResultItem();
        item.setItemValue("  ");

        ExamPackageItem pi = new ExamPackageItem();

        assertFalse(evaluateCondition(rule, item, pi));
    }

    @Test
    void nonNumericValue_shouldReturnFalse() {
        HealthRiskRule rule = new HealthRiskRule();
        rule.setConditionType("gt");
        rule.setThresholdValue("140");

        ExamResultItem item = new ExamResultItem();
        item.setItemValue("ABC");

        ExamPackageItem pi = new ExamPackageItem();

        assertFalse(evaluateCondition(rule, item, pi));
    }
}
