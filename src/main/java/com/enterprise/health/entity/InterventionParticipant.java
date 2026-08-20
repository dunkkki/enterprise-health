package com.enterprise.health.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@TableName("intervention_participant")
@Getter
@Setter
public class InterventionParticipant {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long planId;
    private Long userId;
    private Integer status;
    private LocalDateTime joinedAt;
    private LocalDateTime completedAt;
}
