package com.enterprise.health.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@TableName("health_risk_result")
@Getter
@Setter
public class HealthRiskResult {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long userId;
    private Long recordId;
    private BigDecimal totalScore;
    private String riskLevel;
    private String detailJson;
    private LocalDateTime assessedAt;
}
