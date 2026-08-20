package com.enterprise.health.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@TableName("login_log")
@Getter
@Setter
public class LoginLog {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private String username;
    private Integer status;
    private String failReason;
    private String ip;
    private String userAgent;
    private LocalDateTime createdAt;
}
