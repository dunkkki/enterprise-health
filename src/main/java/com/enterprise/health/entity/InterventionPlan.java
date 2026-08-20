package com.enterprise.health.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@TableName("intervention_plan")
@Getter
@Setter
public class InterventionPlan {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private String title;
    private String type;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
    private Long executorId;
    private Integer status;
    private Long createdBy;
    private LocalDateTime createdAt;
}
