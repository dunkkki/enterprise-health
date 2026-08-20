package com.enterprise.health.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@TableName("follow_up_record")
@Getter
@Setter
public class FollowUpRecord {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long planId;
    private Long userId;
    private LocalDate followDate;
    private String content;
    private String result;
    private LocalDate nextDate;
    private Long recordedBy;
    private LocalDateTime createdAt;
}
