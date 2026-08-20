package com.enterprise.health.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@TableName("exam_record")
@Getter
@Setter
public class ExamRecord {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long userId;
    private Long scheduleId;
    private LocalDate examDate;
    private Integer status;
    private String overallResult;
    private Long updatedBy;
    private LocalDateTime createdAt;
}
