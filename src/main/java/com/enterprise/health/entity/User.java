package com.enterprise.health.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.enterprise.health.common.annotation.Sensitive;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@TableName("user")
@Getter
@Setter
public class User {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private String username;
    @Sensitive
    private String password;
    private String realName;
    private String employeeNo;
    private Integer gender;
    @Sensitive
    private String phone;
    @Sensitive
    private String email;
    private Long deptId;
    private String position;
    private LocalDate hireDate;
    private LocalDate birthDate;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
