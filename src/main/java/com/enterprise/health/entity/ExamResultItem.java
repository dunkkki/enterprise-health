package com.enterprise.health.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

@TableName("exam_result_item")
@Getter
@Setter
public class ExamResultItem {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long recordId;
    private Long packageItemId;
    private String itemValue;
    private Integer isAbnormal;
    private Integer sortOrder;
}
