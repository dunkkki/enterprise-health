package com.enterprise.health.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

@TableName("schedule_dept")
@Getter
@Setter
public class ScheduleDept {
    private Long scheduleId;
    private Long deptId;
}
