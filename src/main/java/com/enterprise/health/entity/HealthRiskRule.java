package com.enterprise.health.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@TableName("health_risk_rule")
@Getter
@Setter
public class HealthRiskRule {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long packageItemId;
    private String ruleName;
    private Integer riskLevel;
    private String conditionType;
    private String thresholdValue;
    private Integer score;
    private BigDecimal weight;
    private Integer status;
}
