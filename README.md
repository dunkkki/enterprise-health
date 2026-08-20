# 企业健康管理平台

面向 200-2000 人规模企业的体检管理 + 健康风险评估 + 干预随访一体化系统。让 HR 从 Excel 手工整理中解放出来，员工可以自助查看体检报告和 AI 健康建议。

## 核心功能

| 模块 | 说明 |
|------|------|
| 数据看板 | 体检完成率、风险等级分布、部门健康排行、干预统计 |
| 组织与员工 | 部门树、员工 CRUD、Excel 批量导入 |
| 体检套餐与排期 | 套餐模板（指标+参考范围）、排期自动生成员工体检记录 |
| 体检记录 | 录入结果自动判断异常、报告详情、打印 |
| 风险评估 | 规则引擎（gt/lt/out_of_range/equals）+ 加权计分，命中明细 JSON |
| 干预随访 | 计划、参与者、随访记录全流程管理 |
| AI 健康建议 | 基于异常指标 + 知识库（RAG）生成个性化建议，AI 干预方案一键转计划 |
| 系统管理 | RBAC 角色/菜单权限、操作日志、登录日志 |

## 技术栈

- **后端**：Spring Boot 3.3.5 + MyBatis-Plus 3.5.7 + Sa-Token 1.38 + MySQL 8 + Redis 7
- **前端**：Vue 3 + Vite + Element Plus + ECharts + Pinia + Vue Router
- **AI**：LangChain4j + 阿里云百炼 DeepSeek（关键词检索 RAG，5 篇健康知识库）

## 项目结构

```
├── src/main/java/com/enterprise/health/
│   ├── controller/    # REST API（15 个 Controller）
│   ├── service/       # 业务逻辑（接口 + impl）
│   ├── mapper/        # MyBatis-Plus Mapper
│   ├── entity/        # 数据实体
│   ├── rag/           # AI 健康建议模块（RAG + DeepSeek）
│   └── common/        # 公共层：config/result/exception/aspect/util
├── src/main/resources/
│   ├── application.yml
│   └── rag/           # 5 篇健康知识库 md
├── frontend/          # Vue 3 前端
├── sql/init.sql       # 建表 + 种子数据
└── docs/              # 需求分析/数据库设计/设计文档/错误日志
```

## 快速启动

**前置**：JDK 17、Maven 3.9+、MySQL 8、Redis、Node 18+

```bash
# 1. 初始化数据库
mysql -uroot -p < sql/init.sql

# 2. 启动 Redis（端口 6379）
E:/Claude/tools/redis/redis-server.exe --port 6379

# 3. 启动后端（localhost:8080）
mvn spring-boot:run

# 4. 启动前端（localhost:5173）
cd frontend && npm run dev
```

**AI 功能**：在 `src/main/resources/application.yml` 配置 `deepseek.api-key`（阿里云百炼）。

## 测试账号

| 账号 | 密码 | 角色 |
|------|------|------|
| admin | admin123 | 超级管理员 |
| hr01 | 123456 | HR 经理 |
| leader01 | 123456 | 部门主管 |
| emp01 | 123456 | 普通员工 |

## 测试

```bash
mvn test   # 23 个单元测试（权限/风险引擎/脱敏/登录日志）
```

## 数据权限

四种角色通过 `role.data_scope` 控制数据可见范围（0=全部 1=本部门 2=仅本人），Service 层用 `DataScopeUtil` 统一过滤，单条详情接口同样校验。

## 设计文档

- `docs/01-需求分析.md`
- `docs/02-数据库设计.md`
- `docs/03-设计文档.md`
- `docs/04-错误日志.md`
- `docs/05-RAG-*.md`（AI 模块设计）
