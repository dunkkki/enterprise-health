package com.enterprise.health.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@TableName("operation_log")
@Getter
@Setter
public class OperationLog {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long userId;
    private String username;
    private String module;
    private String action;
    private String description;
    private String requestMethod;
    private String requestUrl;
    private String requestParams;
    private String ip;
    private Integer duration;
    private LocalDateTime createdAt;
}
