# 企业健康管理平台 — 项目约束

## 技术栈
- 后端：SpringBoot 3.3.5 + MyBatis-Plus 3.5.7（boot3-starter） + MySQL 8.0 + Redis 7.x + Sa-Token 1.38
- Java 17，Maven 3.9+
- 前端：Vue 3 + Vite + Axios + Element Plus + ECharts + Vue Router + Pinia

## 项目结构
- 包路径：com.enterprise.health
- 分层：controller → service（接口）→ service/impl → mapper → entity
- 公共层：common/config（Sa-Token、MyBatis-Plus、跨域、Jackson、Redis）、common/result、common/exception、common/aspect、common/util

## 统一规范
- 响应格式：{ code, msg, data }，用 Result<T> 封装，code=200 成功，400 参数错误，401 未登录，403 无权限，500 服务器错误
- 分页格式：{ total, page, size, records }
- API风格：RESTful，URI 名词复数，前缀 /api
- Controller 类上 @RequestMapping 写路径前缀，方法上写子路径

## 认证与权限
- Sa-Token + Redis 存储 Session
- @SaCheckPermission 注解鉴权（如 @SaCheckPermission("user:create")）
- StpInterfaceImpl 从数据库加载角色和权限列表
- 数据权限：role.data_scope 控制（0=全部 1=本部门 2=仅本人），DataScopeUtil 工具类使用
- 密码 BCrypt 加密

## 数据库
- 库名：enterprise_health，18 张表
- 时区：Asia/Shanghai
- 主键：MyBatis-Plus ASSIGN_ID（雪花算法）
- Jackson Long→String 全局序列化，防 JS 精度丢失
- 逻辑删除：不用，物理删除

## 前端布局 (2026-08 重新设计，基于 superdesign dashboard.md)
- 统一亮色主题：画布 #F8F9FB，卡片 #FFF + 1px 细边框 #E5E8EC
- 管理员：左侧浅色可折叠侧边栏（#F4F5F7）+ 顶部 56px 顶栏 + 面包屑
- HR：横向导航顶栏布局
- Leader/Employee：Tab 式顶栏导航
- 四角色统一琥珀色 accent #E8950A，不再使用多种配色
- KPI 卡片带 delta 对比 + 迷你趋势，tabular-nums 数字
- 图表支持表格降级查看（WCAG AA）
- 色彩无障碍：Okabe-Ito 状态色 + dots/badges 非颜色冗余提示
- 设计令牌：src/assets/tokens.css (CSS 变量统一管理)

## 配色
- 主色 accent：#E8950A（琥珀）
- 画布 canvas：#F8F9FB
- 卡片 surface：#FFFFFF
- 侧边栏：#F4F5F7
- 文字：#1A1D23（主）/#5F6B7A（次）/#8E97A4（辅）
- 状态：绿 #009E73 / 黄 #E69F00 / 橙 #D55E00

## 注意事项 / 易踩坑

### 敏感字段与限流
- `@Sensitive` 注解贴在实体字段上 → Jackson 序列化自动脱敏（common/annotation/Sensitive.java + common/config/serializer/SensitiveSerializer.java）
- 脱敏规则：11 位手机号→138****0001；18 位身份证→前4后4；邮箱→首字母***@域名；其它→首尾各1位打码
- 已标注：User.phone / User.email / User.password / LoginDTO.password。**新增实体敏感字段（手机号/身份证/健康数据）必须补 `@Sensitive`**
- 注意：手动 `Map.put()` 拼返回值的接口不走 Jackson 注解，脱敏不生效——尽量直接返回实体
- `@RateLimit(maxRequests=10, timeWindow=60)` 贴在 Controller 方法上，默认 10 次/60 秒（common/annotation/RateLimit.java）

### 数据权限（易出权限漏洞）
- 数据权限靠 `common/util/DataScopeUtil` 在 service 层拼接 SQL（role.data_scope：0=全部 1=本部门 2=仅本人）
- 新写的 service 查询如果忘了调 DataScopeUtil，用户会越权查到别的部门数据——新增查询必须走它
- **单条详情接口（按 id 查）也必须校验**：scope=0 全量，scope=1 只能看本部门，scope=2 只能看自己（示例：ExamRecordServiceImpl.checkViewPermission）
- 2026-08 修复：records/{id}、rag/health-advice 曾存在员工传他人 recordId 越权看体检数据的漏洞，已加校验

### RAG / AI（阿里云百炼 DeepSeek）
- 服务商：**阿里云百炼**（base-url=https://dashscope.aliyuncs.com/compatible-mode/v1），模型名 `deepseek-v3`
- 模型配置在 rag/config/RagConfig.java，模型名读 `deepseek.model`（application.yml），**不要写死 `deepseek-chat`**（那是官网模型名，百炼不认）
- 知识库：resources/rag/ 下 5 篇 md，启动时按 500 字切片 + 关键词检索（无向量库）
- rag/health-advice 必须校验数据权限（与 records/{id} 同理）；rag/suggest-plan 是管理端功能，需 @SaCheckPermission("risk:assess")
- AI 调用失败返回 500 是正确行为（外部依赖，不泄露细节），别改成静默成功

### 日志切面
- common/aspect 下 4 个切面：WebLogAspect（接口日志）、OperationLogAspect（操作日志）、LoginLogAspect（登录日志）、RateLimitAspect（限流）
- 新增 Controller 方法后确认是否会被 OperationLogAspect 记录、是否需要排除

### 前端按角色分目录
- views 下按角色分：admin / hr / leader / employee / login，新页面必须放进对应角色目录，别放错位置
- 颜色一律引用 `src/assets/tokens.css` 的 CSS 变量，禁止写死色值

### 已知坑：Element Plus CSS 覆盖
- Vite 打包顺序会导致 Element Plus 的 CSS 变量覆盖自定义样式
- 覆盖 Element Plus 样式时，必须用 `!important` + 直接属性值，否则不生效

### Playwright MCP（浏览器截图/验证 UI）
- 已全局注册：`claude mcp add --scope user playwright -- npx @playwright/mcp@latest --allowed-hosts "localhost:5173,localhost:8080" --caps vision --browser chromium`
- **只放行 localhost 开发端口**，`--caps vision` 只开视觉能力不开 devtools
- 权限层 deny：browser_click/type/evaluate/run_code_unsafe/鼠标键盘/上传 等 22 个危险工具（`mcp__playwright__` 前缀，见用户级 settings.json permissions.deny）
- 安全约定：只用测试账号登录、不填真实凭据、用完断开（MCP 是权限最大的工具之一）
- 验证 UI 时：启动前后端 → 让 MCP 打开 localhost:5173 截图

## 启动
- MySQL：localhost:3306, root/qzdqrr20031107
- Redis：E:/Claude/tools/redis/redis-server.exe --port 6379
- 后端：mvn spring-boot:run（localhost:8080）
- 前端：cd frontend && npm run dev（localhost:5173）

## 参考文档
- docs/01-需求分析.md
- docs/02-数据库设计.md
- docs/03-设计文档.md
