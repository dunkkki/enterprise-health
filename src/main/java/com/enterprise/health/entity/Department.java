package com.enterprise.health.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@TableName("department")
@Getter
@Setter
public class Department {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private String name;
    private Long parentId;
    private Integer sortOrder;
    private Integer status;
    private LocalDateTime createdAt;
}
