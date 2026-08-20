package com.enterprise.health.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@TableName("exam_schedule")
@Getter
@Setter
public class ExamSchedule {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private String title;
    private Long packageId;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer status;
    private Long createdBy;
    private LocalDateTime createdAt;
}
