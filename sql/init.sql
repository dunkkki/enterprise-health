CREATE DATABASE IF NOT EXISTS enterprise_health DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE enterprise_health;

-- ============================================================
-- 1. 组织与权限
-- ============================================================

CREATE TABLE department (
    id BIGINT NOT NULL PRIMARY KEY,
    name VARCHAR(64) NOT NULL COMMENT '部门名称',
    parent_id BIGINT NOT NULL DEFAULT 0 COMMENT '父部门ID，顶级为0',
    sort_order INT DEFAULT 0,
    status TINYINT DEFAULT 1 COMMENT '0=禁用 1=启用',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
) COMMENT '部门表';

CREATE TABLE user (
    id BIGINT NOT NULL PRIMARY KEY,
    username VARCHAR(32) NOT NULL UNIQUE COMMENT '登录名',
    password VARCHAR(128) NOT NULL COMMENT 'BCrypt加密',
    real_name VARCHAR(32) COMMENT '真实姓名',
    employee_no VARCHAR(32) UNIQUE COMMENT '工号',
    gender TINYINT DEFAULT 0 COMMENT '0=女 1=男',
    phone VARCHAR(20),
    email VARCHAR(64),
    dept_id BIGINT COMMENT 'FK→department.id',
    position VARCHAR(32) COMMENT '岗位',
    hire_date DATE COMMENT '入职日期',
    birth_date DATE COMMENT '出生日期',
    status TINYINT DEFAULT 1 COMMENT '0=禁用 1=启用',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) COMMENT '用户表';

CREATE TABLE role (
    id BIGINT NOT NULL PRIMARY KEY,
    name VARCHAR(32) NOT NULL COMMENT '角色名',
    code VARCHAR(32) NOT NULL UNIQUE COMMENT '角色编码',
    description VARCHAR(128),
    data_scope TINYINT DEFAULT 2 COMMENT '0=全部 1=本部门 2=仅本人',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
) COMMENT '角色表';

CREATE TABLE user_role (
    id BIGINT NOT NULL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    UNIQUE KEY uk_user_role (user_id, role_id)
) COMMENT '用户角色关联';

CREATE TABLE menu (
    id BIGINT NOT NULL PRIMARY KEY,
    name VARCHAR(32) NOT NULL,
    parent_id BIGINT DEFAULT 0,
    path VARCHAR(128) COMMENT '前端路由路径',
    icon VARCHAR(32),
    sort_order INT DEFAULT 0,
    type TINYINT DEFAULT 1 COMMENT '0=目录 1=菜单 2=按钮',
    permission VARCHAR(64) COMMENT '权限标识',
    visible TINYINT DEFAULT 1 COMMENT '0=隐藏 1=显示'
) COMMENT '菜单表';

CREATE TABLE role_menu (
    id BIGINT NOT NULL PRIMARY KEY,
    role_id BIGINT NOT NULL,
    menu_id BIGINT NOT NULL,
    UNIQUE KEY uk_role_menu (role_id, menu_id)
) COMMENT '角色菜单关联';

-- ============================================================
-- 2. 体检管理
-- ============================================================

CREATE TABLE exam_package (
    id BIGINT NOT NULL PRIMARY KEY,
    name VARCHAR(64) NOT NULL COMMENT '套餐名称',
    description VARCHAR(256),
    applicable_gender TINYINT DEFAULT 2 COMMENT '0=女 1=男 2=不限',
    price DECIMAL(10,2) DEFAULT 0,
    status TINYINT DEFAULT 1 COMMENT '0=停用 1=启用',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) COMMENT '体检套餐';

CREATE TABLE exam_package_item (
    id BIGINT NOT NULL PRIMARY KEY,
    package_id BIGINT NOT NULL,
    item_name VARCHAR(64) NOT NULL COMMENT '指标名称',
    item_category VARCHAR(32) COMMENT '类别',
    unit VARCHAR(16) COMMENT '单位',
    ref_min DECIMAL(10,2) COMMENT '参考下限',
    ref_max DECIMAL(10,2) COMMENT '参考上限',
    sort_order INT DEFAULT 0,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) COMMENT '套餐项目';

CREATE TABLE exam_schedule (
    id BIGINT NOT NULL PRIMARY KEY,
    title VARCHAR(128) NOT NULL COMMENT '排期标题',
    package_id BIGINT NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    status TINYINT DEFAULT 0 COMMENT '0=未开始 1=进行中 2=已截止',
    created_by BIGINT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CHECK (end_date >= start_date)
) COMMENT '体检排期';

CREATE TABLE exam_record (
    id BIGINT NOT NULL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    schedule_id BIGINT NOT NULL,
    exam_date DATE COMMENT '实际体检日期',
    status TINYINT DEFAULT 0 COMMENT '0=未检 1=已检 2=请假',
    overall_result VARCHAR(64) COMMENT '综合结论',
    updated_by BIGINT COMMENT '录入人 FK→user.id',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_schedule (user_id, schedule_id)
) COMMENT '体检记录';

CREATE TABLE exam_result_item (
    id BIGINT NOT NULL PRIMARY KEY,
    record_id BIGINT NOT NULL,
    package_item_id BIGINT NOT NULL COMMENT 'FK→exam_package_item.id',
    item_value VARCHAR(32) COMMENT '检测值',
    is_abnormal TINYINT DEFAULT 0 COMMENT '0=正常 1=偏高 2=偏低',
    sort_order INT DEFAULT 0
) COMMENT '体检结果明细';

-- 排期-部门关联（多对多）
CREATE TABLE schedule_dept (
    schedule_id BIGINT NOT NULL,
    dept_id BIGINT NOT NULL,
    PRIMARY KEY (schedule_id, dept_id)
) COMMENT '排期-部门关联';

-- ============================================================
-- 3. 风险评估
-- ============================================================

CREATE TABLE health_risk_rule (
    id BIGINT NOT NULL PRIMARY KEY,
    package_item_id BIGINT NOT NULL COMMENT 'FK→exam_package_item.id',
    rule_name VARCHAR(64) NOT NULL,
    risk_level TINYINT NOT NULL COMMENT '1=低 2=中 3=高',
    condition_type VARCHAR(16) NOT NULL COMMENT 'gt/lt/out_of_range/equals',
    threshold_value VARCHAR(32) COMMENT '自定义阈值',
    score INT DEFAULT 0 COMMENT '风险分数',
    weight DECIMAL(3,2) DEFAULT 1.00,
    status TINYINT DEFAULT 1 COMMENT '0=禁用 1=启用'
) COMMENT '风险评估规则';

CREATE TABLE health_risk_result (
    id BIGINT NOT NULL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    record_id BIGINT NOT NULL,
    total_score DECIMAL(5,1) DEFAULT 0,
    risk_level VARCHAR(8) COMMENT '低/中/高',
    detail_json TEXT COMMENT '规则命中明细JSON',
    assessed_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_record (record_id) COMMENT '一条体检记录只有一次评估结果'
) COMMENT '风险评估结果';

-- ============================================================
-- 4. 干预随访
-- ============================================================

CREATE TABLE intervention_plan (
    id BIGINT NOT NULL PRIMARY KEY,
    title VARCHAR(128) NOT NULL,
    type VARCHAR(32) NOT NULL COMMENT 'lecture/exercise/weight/smoking/mental',
    description TEXT,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    executor_id BIGINT COMMENT '执行人',
    status TINYINT DEFAULT 0 COMMENT '0=未开始 1=进行中 2=已结束',
    created_by BIGINT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CHECK (end_date >= start_date)
) COMMENT '干预计划';

CREATE TABLE intervention_participant (
    id BIGINT NOT NULL PRIMARY KEY,
    plan_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    status TINYINT DEFAULT 0 COMMENT '0=待开始 1=进行中 2=已完成 3=已退出',
    joined_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    completed_at DATETIME,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_plan_user (plan_id, user_id)
) COMMENT '干预参与';

CREATE TABLE follow_up_record (
    id BIGINT NOT NULL PRIMARY KEY,
    plan_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    follow_date DATE NOT NULL COMMENT '随访日期',
    content TEXT COMMENT '随访内容',
    result VARCHAR(32) COMMENT '好转/稳定/恶化/未联系上',
    next_date DATE COMMENT '下次随访日期',
    recorded_by BIGINT COMMENT '记录人',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) COMMENT '随访记录';

-- ============================================================
-- 5. 系统日志
-- ============================================================

CREATE TABLE operation_log (
    id BIGINT NOT NULL PRIMARY KEY,
    user_id BIGINT,
    username VARCHAR(32),
    module VARCHAR(32),
    action VARCHAR(64),
    description VARCHAR(256),
    request_method VARCHAR(8),
    request_url VARCHAR(256),
    request_params TEXT,
    ip VARCHAR(64),
    duration INT COMMENT '耗时ms',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
) COMMENT '操作日志';

CREATE TABLE login_log (
    id BIGINT NOT NULL PRIMARY KEY,
    username VARCHAR(32),
    status TINYINT DEFAULT 1 COMMENT '0=失败 1=成功',
    fail_reason VARCHAR(64),
    ip VARCHAR(64),
    user_agent VARCHAR(256),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
) COMMENT '登录日志';

-- ============================================================
-- 外键约束
-- ============================================================

-- 用户 → 部门
ALTER TABLE user ADD CONSTRAINT fk_user_dept
    FOREIGN KEY (dept_id) REFERENCES department(id) ON DELETE SET NULL;

-- 用户角色关联
ALTER TABLE user_role ADD CONSTRAINT fk_ur_user
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE;
ALTER TABLE user_role ADD CONSTRAINT fk_ur_role
    FOREIGN KEY (role_id) REFERENCES role(id) ON DELETE CASCADE;

-- 角色菜单关联
ALTER TABLE role_menu ADD CONSTRAINT fk_rm_role
    FOREIGN KEY (role_id) REFERENCES role(id) ON DELETE CASCADE;
ALTER TABLE role_menu ADD CONSTRAINT fk_rm_menu
    FOREIGN KEY (menu_id) REFERENCES menu(id) ON DELETE CASCADE;

-- 套餐项目 → 套餐
ALTER TABLE exam_package_item ADD CONSTRAINT fk_pi_package
    FOREIGN KEY (package_id) REFERENCES exam_package(id) ON DELETE CASCADE;

-- 排期 → 套餐
ALTER TABLE exam_schedule ADD CONSTRAINT fk_sched_package
    FOREIGN KEY (package_id) REFERENCES exam_package(id) ON DELETE RESTRICT;
ALTER TABLE exam_schedule ADD CONSTRAINT fk_sched_creator
    FOREIGN KEY (created_by) REFERENCES user(id) ON DELETE SET NULL;

-- 排期-部门关联
ALTER TABLE schedule_dept ADD CONSTRAINT fk_sd_schedule
    FOREIGN KEY (schedule_id) REFERENCES exam_schedule(id) ON DELETE CASCADE;
ALTER TABLE schedule_dept ADD CONSTRAINT fk_sd_dept
    FOREIGN KEY (dept_id) REFERENCES department(id) ON DELETE CASCADE;

-- 体检记录 → 用户 / 排期
ALTER TABLE exam_record ADD CONSTRAINT fk_record_user
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE;
ALTER TABLE exam_record ADD CONSTRAINT fk_record_schedule
    FOREIGN KEY (schedule_id) REFERENCES exam_schedule(id) ON DELETE CASCADE;

-- 结果明细 → 记录 / 套餐项目模板
ALTER TABLE exam_result_item ADD CONSTRAINT fk_eri_record
    FOREIGN KEY (record_id) REFERENCES exam_record(id) ON DELETE CASCADE;
ALTER TABLE exam_result_item ADD CONSTRAINT fk_eri_item
    FOREIGN KEY (package_item_id) REFERENCES exam_package_item(id) ON DELETE RESTRICT;

-- 风险规则 → 套餐项目模板
ALTER TABLE health_risk_rule ADD CONSTRAINT fk_rule_item
    FOREIGN KEY (package_item_id) REFERENCES exam_package_item(id) ON DELETE CASCADE;

-- 风险评估结果 → 用户 / 体检记录
ALTER TABLE health_risk_result ADD CONSTRAINT fk_risk_user
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE;
ALTER TABLE health_risk_result ADD CONSTRAINT fk_risk_record
    FOREIGN KEY (record_id) REFERENCES exam_record(id) ON DELETE CASCADE;

-- 干预计划 → 执行人 / 创建人
ALTER TABLE intervention_plan ADD CONSTRAINT fk_ip_executor
    FOREIGN KEY (executor_id) REFERENCES user(id) ON DELETE SET NULL;
ALTER TABLE intervention_plan ADD CONSTRAINT fk_ip_creator
    FOREIGN KEY (created_by) REFERENCES user(id) ON DELETE SET NULL;

-- 干预参与 → 计划 / 用户
ALTER TABLE intervention_participant ADD CONSTRAINT fk_ipart_plan
    FOREIGN KEY (plan_id) REFERENCES intervention_plan(id) ON DELETE CASCADE;
ALTER TABLE intervention_participant ADD CONSTRAINT fk_ipart_user
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE;

-- 随访记录 → 计划 / 用户 / 记录人
ALTER TABLE follow_up_record ADD CONSTRAINT fk_fu_plan
    FOREIGN KEY (plan_id) REFERENCES intervention_plan(id) ON DELETE CASCADE;
ALTER TABLE follow_up_record ADD CONSTRAINT fk_fu_user
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE;
ALTER TABLE follow_up_record ADD CONSTRAINT fk_fu_recorder
    FOREIGN KEY (recorded_by) REFERENCES user(id) ON DELETE SET NULL;

-- 操作日志 → 用户（SET NULL，日志不随用户删除）
ALTER TABLE operation_log ADD CONSTRAINT fk_olog_user
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE SET NULL;

-- login_log 无外键，独立审计表

-- ============================================================
-- 种子数据
-- ============================================================

-- === 部门 ===
INSERT INTO department (id, name, parent_id, sort_order) VALUES
(1, '总公司', 0, 1),
(2, '技术部', 1, 1),
(3, '人力资源部', 1, 2),
(4, '市场部', 1, 3),
(5, '财务部', 1, 4);

-- === 角色 ===
INSERT INTO role (id, name, code, description, data_scope) VALUES
(1, '超级管理员', 'admin', '系统全部权限', 0),
(2, 'HR经理', 'hr', '员工管理、体检管理、报表查看', 0),
(3, '部门主管', 'leader', '本部门数据查看', 1),
(4, '普通员工', 'employee', '仅本人数据', 2);

-- === 菜单（树形结构） ===
-- 目录级
INSERT INTO menu (id, name, parent_id, path, icon, sort_order, type, permission, visible) VALUES
(1, '数据看板', 0, '/dashboard', 'DataAnalysis', 1, 0, NULL, 1),
(2, '组织架构', 0, '/org', 'OfficeBuilding', 2, 0, NULL, 1),
(3, '体检管理', 0, '/exam', 'FirstAidKit', 3, 0, NULL, 1),
(4, '风险评估', 0, '/risk', 'Warning', 4, 0, NULL, 1),
(5, '干预管理', 0, '/intervention', 'Clock', 5, 0, NULL, 1),
(6, '系统管理', 0, '/system', 'Setting', 99, 0, NULL, 1),
(7, '个人中心', 0, '/my', 'User', 100, 0, NULL, 1);
-- 菜单级
INSERT INTO menu (id, name, parent_id, path, icon, sort_order, type, permission, visible) VALUES
(11, '数据看板', 1, '/dashboard', NULL, 1, 1, NULL, 1),
(21, '部门管理', 2, '/org/dept', NULL, 1, 1, 'dept:list', 1),
(22, '员工管理', 2, '/org/user', NULL, 2, 1, 'user:list', 1),
(31, '体检套餐', 3, '/exam/package', NULL, 1, 1, 'package:list', 1),
(32, '体检排期', 3, '/exam/schedule', NULL, 2, 1, 'schedule:list', 1),
(33, '体检记录', 3, '/exam/record', NULL, 3, 1, 'record:list', 1),
(41, '评估规则', 4, '/risk/rules', NULL, 1, 1, 'risk:list', 1),
(42, '评估结果', 4, '/risk/results', NULL, 2, 1, 'risk:list', 1),
(51, '干预计划', 5, '/intervention/plan', NULL, 1, 1, 'intervention:list', 1),
(52, '随访记录', 5, '/intervention/follow', NULL, 2, 1, 'followup:list', 1),
(61, '角色管理', 6, '/system/role', NULL, 1, 1, 'role:list', 1),
(62, '菜单管理', 6, '/system/menu', NULL, 2, 1, 'menu:list', 1),
(63, '操作日志', 6, '/system/log', NULL, 3, 1, 'log:list', 1),
(71, '我的体检', 7, '/my/exams', NULL, 1, 1, NULL, 1),
(72, '我的风险', 7, '/my/risks', NULL, 2, 1, NULL, 1),
(73, '我的干预', 7, '/my/interventions', NULL, 3, 1, NULL, 1),
(74, '个人信息', 7, '/my/profile', NULL, 4, 1, NULL, 1);
-- 按钮级权限（不可见，仅用于后端鉴权）
INSERT INTO menu (id, name, parent_id, path, icon, sort_order, type, permission, visible) VALUES
(200, '新增部门', 21, NULL, NULL, 1, 2, 'dept:create', 0),
(201, '编辑部门', 21, NULL, NULL, 2, 2, 'dept:update', 0),
(202, '删除部门', 21, NULL, NULL, 3, 2, 'dept:delete', 0),
(203, '新增用户', 22, NULL, NULL, 1, 2, 'user:create', 0),
(204, '编辑用户', 22, NULL, NULL, 2, 2, 'user:update', 0),
(205, '删除用户', 22, NULL, NULL, 3, 2, 'user:delete', 0),
(206, '导入用户', 22, NULL, NULL, 4, 2, 'user:import', 0),
(207, '新增套餐', 31, NULL, NULL, 1, 2, 'package:create', 0),
(208, '编辑套餐', 31, NULL, NULL, 2, 2, 'package:update', 0),
(209, '删除套餐', 31, NULL, NULL, 3, 2, 'package:delete', 0),
(210, '创建排期', 32, NULL, NULL, 1, 2, 'schedule:create', 0),
(211, '编辑排期', 32, NULL, NULL, 2, 2, 'schedule:update', 0),
(212, '录入体检', 33, NULL, NULL, 1, 2, 'record:create', 0),
(213, '新增规则', 41, NULL, NULL, 1, 2, 'risk:create', 0),
(214, '编辑规则', 41, NULL, NULL, 2, 2, 'risk:update', 0),
(215, '删除规则', 41, NULL, NULL, 3, 2, 'risk:delete', 0),
(216, '执行评估', 42, NULL, NULL, 1, 2, 'risk:assess', 0),
(217, '创建计划', 51, NULL, NULL, 1, 2, 'intervention:create', 0),
(218, '编辑计划', 51, NULL, NULL, 2, 2, 'intervention:update', 0),
(219, '删除计划', 51, NULL, NULL, 3, 2, 'intervention:delete', 0),
(220, '新增随访', 52, NULL, NULL, 1, 2, 'followup:create', 0),
(221, '编辑随访', 52, NULL, NULL, 2, 2, 'followup:update', 0),
(222, '新增角色', 61, NULL, NULL, 1, 2, 'role:create', 0),
(223, '编辑角色', 61, NULL, NULL, 2, 2, 'role:update', 0),
(224, '删除角色', 61, NULL, NULL, 3, 2, 'role:delete', 0),
(225, '新增菜单', 62, NULL, NULL, 1, 2, 'menu:create', 0),
(226, '编辑菜单', 62, NULL, NULL, 2, 2, 'menu:update', 0),
(227, '删除菜单', 62, NULL, NULL, 3, 2, 'menu:delete', 0);

-- === 角色-菜单关联 ===
-- admin: 所有菜单+按钮
INSERT INTO role_menu (id, role_id, menu_id)
SELECT id + 1000, 1, id FROM menu;
-- hr: 除系统管理外的所有菜单
INSERT INTO role_menu (id, role_id, menu_id)
SELECT id + 3000, 2, id FROM menu WHERE parent_id IN (1,2,3,4,5,7) OR id IN (1,2,3,4,5,7);
INSERT INTO role_menu (id, role_id, menu_id)
SELECT id + 4000, 2, id FROM menu WHERE type = 2 AND permission IN
  ('dept:create','dept:update','dept:delete','user:create','user:update','user:delete','user:import',
   'package:create','package:update','package:delete','schedule:create','schedule:update',
   'record:create','risk:create','risk:update','risk:delete','risk:assess',
   'intervention:create','intervention:update','intervention:delete','followup:create','followup:update');
-- leader: 数据看板 + 风险评估 + 个人中心
INSERT INTO role_menu (id, role_id, menu_id) VALUES
(5001, 3, 1), (5002, 3, 11), (5003, 3, 4), (5004, 3, 42),
(5005, 3, 7), (5006, 3, 71), (5007, 3, 72), (5008, 3, 73), (5009, 3, 74);
-- employee: 个人中心
INSERT INTO role_menu (id, role_id, menu_id) VALUES
(6001, 4, 7), (6002, 4, 71), (6003, 4, 72), (6004, 4, 73), (6005, 4, 74);


-- === 用户（BCrypt密码） ===
-- admin123 → $2b$12$DCK1DJtvfJeUH7iv.ITsju8YeBXOm42ihFpvAQIeIogLCy3T/95ES
-- 123456   → $2b$12$vxEyCytGy9mG0nAbPFT7kuPIplpYWtCG4WDVv3VK6LyUYdaRssDd2
INSERT INTO user (id, username, password, real_name, employee_no, gender, phone, email, dept_id, position, hire_date, birth_date, status) VALUES
(1, 'admin', '$2b$12$DCK1DJtvfJeUH7iv.ITsju8YeBXOm42ihFpvAQIeIogLCy3T/95ES', '系统管理员', 'E001', 1, '13800000001', 'admin@company.com', 1, '系统管理员', '2020-01-01', '1985-06-15', 1),
(2, 'hr01', '$2b$12$vxEyCytGy9mG0nAbPFT7kuPIplpYWtCG4WDVv3VK6LyUYdaRssDd2', '张人事', 'E002', 0, '13800000002', 'hr01@company.com', 3, 'HR经理', '2021-03-15', '1990-08-20', 1),
(3, 'leader01', '$2b$12$vxEyCytGy9mG0nAbPFT7kuPIplpYWtCG4WDVv3VK6LyUYdaRssDd2', '李技术', 'E003', 1, '13800000003', 'leader01@company.com', 2, '技术总监', '2021-06-01', '1988-11-10', 1),
(4, 'emp01', '$2b$12$vxEyCytGy9mG0nAbPFT7kuPIplpYWtCG4WDVv3VK6LyUYdaRssDd2', '王员工', 'E004', 1, '13800000004', 'emp01@company.com', 2, '软件工程师', '2022-01-10', '1995-03-25', 1),
(5, 'emp02', '$2b$12$vxEyCytGy9mG0nAbPFT7kuPIplpYWtCG4WDVv3VK6LyUYdaRssDd2', '赵员工', 'E005', 0, '13800000005', 'emp02@company.com', 3, 'HR专员', '2022-05-20', '1997-07-12', 1),
(6, 'emp03', '$2b$12$vxEyCytGy9mG0nAbPFT7kuPIplpYWtCG4WDVv3VK6LyUYdaRssDd2', '钱员工', 'E006', 1, '13800000006', 'emp03@company.com', 4, '市场专员', '2022-08-15', '1996-01-30', 1),
(7, 'emp04', '$2b$12$vxEyCytGy9mG0nAbPFT7kuPIplpYWtCG4WDVv3VK6LyUYdaRssDd2', '孙员工', 'E007', 0, '13800000007', 'emp04@company.com', 5, '会计', '2023-02-01', '1994-09-18', 1);

-- === 用户-角色 ===
INSERT INTO user_role (id, user_id, role_id) VALUES
(1, 1, 1),
(2, 2, 2),
(3, 3, 3),
(4, 4, 4),
(5, 5, 4),
(6, 6, 4),
(7, 7, 4);

-- === 体检套餐 ===
INSERT INTO exam_package (id, name, description, applicable_gender, price, status) VALUES
(1, '标准入职体检', '包含基础内外科、血常规、尿常规、心电图、胸部X光', 2, 350.00, 1),
(2, '年度综合体检', '包含全套血液生化、影像学检查、肿瘤标志物筛查', 2, 1200.00, 1),
(3, '女性专项体检', '在年度综合体检基础上增加妇科检查、乳腺彩超', 0, 1600.00, 1);

-- === 套餐项目 ===
-- 标准入职体检项目
INSERT INTO exam_package_item (id, package_id, item_name, item_category, unit, ref_min, ref_max, sort_order) VALUES
(101, 1, '收缩压', '内科', 'mmHg', 90, 140, 1),
(102, 1, '舒张压', '内科', 'mmHg', 60, 90, 2),
(103, 1, '心率', '内科', '次/分', 60, 100, 3),
(104, 1, '白细胞计数', '血液', '×10⁹/L', 4.0, 10.0, 4),
(105, 1, '红细胞计数', '血液', '×10¹²/L', 4.0, 5.5, 5),
(106, 1, '血红蛋白', '血液', 'g/L', 120, 160, 6),
(107, 1, '空腹血糖', '血液', 'mmol/L', 3.9, 6.1, 7),
(108, 1, '总胆固醇', '血液', 'mmol/L', 2.8, 5.2, 8),
(109, 1, '谷丙转氨酶', '血液', 'U/L', 0, 40, 9),
(110, 1, '尿蛋白', '尿液', NULL, NULL, NULL, 10),
(111, 1, '心电图', '影像', NULL, NULL, NULL, 11);

-- 年度综合体检项目
INSERT INTO exam_package_item (id, package_id, item_name, item_category, unit, ref_min, ref_max, sort_order) VALUES
(201, 2, '收缩压', '内科', 'mmHg', 90, 140, 1),
(202, 2, '舒张压', '内科', 'mmHg', 60, 90, 2),
(203, 2, 'BMI', '内科', 'kg/m²', 18.5, 24, 3),
(204, 2, '白细胞计数', '血液', '×10⁹/L', 4.0, 10.0, 4),
(205, 2, '空腹血糖', '血液', 'mmol/L', 3.9, 6.1, 5),
(206, 2, '总胆固醇', '血液', 'mmol/L', 2.8, 5.2, 6),
(207, 2, '甘油三酯', '血液', 'mmol/L', 0.56, 1.7, 7),
(208, 2, '高密度脂蛋白', '血液', 'mmol/L', 1.0, 2.0, 8),
(209, 2, '低密度脂蛋白', '血液', 'mmol/L', 0, 3.4, 9),
(210, 2, '谷丙转氨酶', '血液', 'U/L', 0, 40, 10),
(211, 2, '肌酐', '血液', 'μmol/L', 44, 133, 11),
(212, 2, '尿酸', '血液', 'μmol/L', 150, 420, 12),
(213, 2, '甲胎蛋白', '血液', 'ng/mL', 0, 7, 13),
(214, 2, '癌胚抗原', '血液', 'ng/mL', 0, 5, 14),
(215, 2, '心电图', '影像', NULL, NULL, NULL, 15),
(216, 2, '胸部X光', '影像', NULL, NULL, NULL, 16),
(217, 2, '腹部B超', '影像', NULL, NULL, NULL, 17);


-- === 体检排期（3个：2个已完成用于历史趋势 + 1个进行中） ===
INSERT INTO exam_schedule (id, title, package_id, start_date, end_date, status, created_by) VALUES
(1, '2024年度全员体检', 2, '2024-03-01', '2024-04-30', 2, 1),
(2, '2025年度全员体检', 2, '2025-03-01', '2025-04-30', 2, 1),
(3, '2026年度全员体检', 2, '2026-07-01', '2026-09-30', 1, 1);

-- === 排期-部门关联 ===
INSERT INTO schedule_dept (schedule_id, dept_id) VALUES
(1, 2), (1, 3), (1, 4), (1, 5),
(2, 2), (2, 3), (2, 4), (2, 5),
(3, 2), (3, 3), (3, 4), (3, 5);

-- === 体检记录（2024 + 2025 历史数据 + 2026 部分数据） ===
-- 2024年（全部已检）
INSERT INTO exam_record (id, user_id, schedule_id, exam_date, status, overall_result) VALUES
(1001, 4, 1, '2024-03-10', 1, '正常'),
(1002, 5, 1, '2024-03-12', 1, '正常'),
(1003, 6, 1, '2024-03-15', 1, '异常'),
(1004, 7, 1, '2024-03-18', 1, '正常');
-- 2025年（全部已检）
INSERT INTO exam_record (id, user_id, schedule_id, exam_date, status, overall_result) VALUES
(2001, 4, 2, '2025-03-08', 1, '正常'),
(2002, 5, 2, '2025-03-10', 1, '正常'),
(2003, 6, 2, '2025-03-14', 1, '建议复查'),
(2004, 7, 2, '2025-03-20', 1, '正常');
-- 2026年（2人已检，2人未检）
INSERT INTO exam_record (id, user_id, schedule_id, exam_date, status, overall_result) VALUES
(3001, 4, 3, '2026-07-10', 1, '正常'),
(3002, 5, 3, '2026-07-12', 1, '异常'),
(3003, 6, 3, NULL, 0, NULL),
(3004, 7, 3, NULL, 0, NULL);

-- === 体检结果明细（仅2026年数据，用于演示） ===
-- emp01 (王员工) 2026年体检结果 — 全部正常
INSERT INTO exam_result_item (id, record_id, package_item_id, item_value, is_abnormal, sort_order) VALUES
(4001, 3001, 201, '118', 0, 1),
(4002, 3001, 202, '76', 0, 2),
(4003, 3001, 203, '22.5', 0, 3),
(4004, 3001, 204, '6.8', 0, 4),
(4005, 3001, 205, '5.2', 0, 5),
(4006, 3001, 206, '4.8', 0, 6),
(4007, 3001, 207, '1.2', 0, 7),
(4008, 3001, 208, '1.5', 0, 8),
(4009, 3001, 209, '2.8', 0, 9),
(4010, 3001, 210, '22', 0, 10),
(4011, 3001, 211, '78', 0, 11),
(4012, 3001, 212, '320', 0, 12),
(4013, 3001, 213, '3.2', 0, 13),
(4014, 3001, 214, '2.1', 0, 14);

-- emp02 (赵员工) 2026年体检结果 — 多项异常
INSERT INTO exam_result_item (id, record_id, package_item_id, item_value, is_abnormal, sort_order) VALUES
(4101, 3002, 201, '148', 1, 1),
(4102, 3002, 202, '95', 1, 2),
(4103, 3002, 203, '27.5', 1, 3),
(4104, 3002, 204, '7.2', 0, 4),
(4105, 3002, 205, '7.8', 1, 5),
(4106, 3002, 206, '6.1', 1, 6),
(4107, 3002, 207, '2.3', 1, 7),
(4108, 3002, 208, '0.9', 1, 8),
(4109, 3002, 209, '4.1', 1, 9),
(4110, 3002, 210, '45', 1, 10),
(4111, 3002, 211, '88', 0, 11),
(4112, 3002, 212, '450', 1, 12),
(4113, 3002, 213, '4.5', 0, 13),
(4114, 3002, 214, '3.0', 0, 14);

-- === 风险评估规则 ===
INSERT INTO health_risk_rule (id, package_item_id, rule_name, risk_level, condition_type, threshold_value, score, weight, status) VALUES
-- 收缩压 (201)
(501, 201, '收缩压轻度偏高', 1, 'gt', '140', 5, 0.5, 1),
(502, 201, '收缩压中度偏高', 2, 'gt', '150', 15, 1.0, 1),
(503, 201, '收缩压重度偏高', 3, 'gt', '170', 30, 1.0, 1),
-- 舒张压 (202)
(504, 202, '舒张压轻度偏高', 1, 'gt', '90', 5, 0.5, 1),
(505, 202, '舒张压中度偏高', 2, 'gt', '100', 15, 1.0, 1),
-- BMI (203)
(506, 203, 'BMI超重', 1, 'gt', '24', 5, 0.5, 1),
(507, 203, 'BMI肥胖', 2, 'gt', '28', 20, 1.0, 1),
-- 空腹血糖 (205)
(508, 205, '血糖偏高', 2, 'gt', '6.1', 15, 1.0, 1),
(509, 205, '血糖严重偏高', 3, 'gt', '10.0', 30, 1.0, 1),
-- 总胆固醇 (206)
(510, 206, '胆固醇偏高', 2, 'gt', '5.2', 15, 1.0, 1),
-- 甘油三酯 (207)
(511, 207, '甘油三酯偏高', 2, 'gt', '1.7', 10, 1.0, 1),
-- HDL (208, 偏低方向)
(512, 208, 'HDL偏低', 2, 'lt', '1.0', 10, 1.0, 1),
-- LDL (209)
(513, 209, 'LDL偏高', 2, 'gt', '3.4', 10, 1.0, 1),
-- ALT (210)
(514, 210, 'ALT偏高', 2, 'gt', '40', 10, 0.8, 1),
-- 尿酸 (212)
(515, 212, '尿酸偏高', 2, 'gt', '420', 10, 1.0, 1);

-- === 风险评估结果（对2026年已检的2人） ===
INSERT INTO health_risk_result (id, user_id, record_id, total_score, risk_level, detail_json, assessed_at) VALUES
(1, 4, 3001, 0, '低', '[{"rule":"无异常指标","score":0}]', '2026-07-10 14:00:00'),
(2, 5, 3002, 75.5, '高', '[{"rule":"收缩压中度偏高","score":15},{"rule":"舒张压轻度偏高","score":2.5},{"rule":"BMI肥胖","score":20},{"rule":"血糖偏高","score":15},{"rule":"胆固醇偏高","score":15},{"rule":"甘油三酯偏高","score":10},{"rule":"HDL偏低","score":10},{"rule":"LDL偏高","score":10},{"rule":"ALT偏高","score":8},{"rule":"尿酸偏高","score":10}]', '2026-07-12 15:30:00');

-- === 干预计划 ===
INSERT INTO intervention_plan (id, title, type, description, start_date, end_date, executor_id, status, created_by) VALUES
(1, '2026年度减重训练营', 'weight', '针对BMI超标员工，为期12周减重计划，每周2次集体训练+营养师指导', '2026-08-01', '2026-10-31', 2, 1, 2),
(2, '办公族颈椎健康讲座', 'lecture', '邀请康复科医生进行颈椎保健知识讲座，教授日常拉伸放松技巧', '2026-08-15', '2026-08-15', 2, 0, 2),
(3, '戒烟互助小组', 'smoking', '为期6个月戒烟互助计划，每周一次小组会+CO检测', '2026-09-01', '2027-02-28', 3, 0, 2);

-- === 干预参与 ===
INSERT INTO intervention_participant (id, plan_id, user_id, status) VALUES
(1, 1, 5, 1),    -- 赵员工 参与减重
(2, 1, 6, 1);    -- 钱员工 参与减重

-- === 随访记录 ===
INSERT INTO follow_up_record (id, plan_id, user_id, follow_date, content, result, recorded_by) VALUES
(1, 1, 5, '2026-08-08', '首周体重90kg，已参加2次训练，饮食记录完成良好', '好转', 2),
(2, 1, 5, '2026-08-15', '第二周体重88.5kg，运动耐力提升，建议增加有氧时间', '好转', 2),
(3, 1, 6, '2026-08-08', '首周体重78kg，训练参与积极，饮食控制良好', '稳定', 2);

-- === 操作日志（示例数据） ===
INSERT INTO operation_log (id, user_id, username, module, action, description, request_method, request_url, ip, duration) VALUES
(1, 1, 'admin', '系统管理', '登录', '管理员登录系统', 'POST', '/api/auth/login', '127.0.0.1', 120),
(2, 2, 'hr01', '员工管理', '查询', '查询员工列表', 'GET', '/api/users?page=1&size=10', '127.0.0.1', 85),
(3, 2, 'hr01', '体检管理', '录入', '录入赵员工2026年体检结果', 'POST', '/api/records', '127.0.0.1', 350);
