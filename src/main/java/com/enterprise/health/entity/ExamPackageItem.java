package com.enterprise.health.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@TableName("exam_package_item")
@Getter
@Setter
public class ExamPackageItem {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long packageId;
    private String itemName;
    private String itemCategory;
    private String unit;
    private BigDecimal refMin;
    private BigDecimal refMax;
    private Integer sortOrder;
}
