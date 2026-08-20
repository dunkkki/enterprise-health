package com.enterprise.health.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

@TableName("menu")
@Getter
@Setter
public class Menu {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private String name;
    private Long parentId;
    private String path;
    private String icon;
    private Integer sortOrder;
    private Integer type;
    private String permission;
    private Integer visible;
}
