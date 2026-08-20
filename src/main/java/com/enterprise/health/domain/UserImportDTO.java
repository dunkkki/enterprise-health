package com.enterprise.health.domain;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserImportDTO {
    @ExcelProperty("用户名")
    private String username;
    @ExcelProperty("真实姓名")
    private String realName;
    @ExcelProperty("工号")
    private String employeeNo;
    @ExcelProperty("性别")
    private String gender;
    @ExcelProperty("手机号")
    private String phone;
    @ExcelProperty("邮箱")
    private String email;
    @ExcelProperty("部门ID")
    private Long deptId;
    @ExcelProperty("岗位")
    private String position;
}
