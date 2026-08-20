# AI 智能健康建议 — 设计文档

## 1. 技术选型

| 层 | 技术 | 版本 | 选型理由 |
|------|------|------|------|
| LLM 框架 | LangChain4j | 1.0.0-beta1 | Java 生态最主流 LLM 框架，2026 年活跃 |
| 大模型 | DeepSeek | API | 和项目共用，无需额外申请 |
| 向量数据库 | PgVector | 0.8.0 | PostgreSQL 原生向量扩展，和 MySQL 并行 |
| 文档解析 | Apache Tika | 3.1.0 | 支持 PDF/Word/TXT，Java 生态标准选择 |
| 后端 | Spring Boot | 3.3.5 | 和主项目一致 |

### 为什么引入 PostgreSQL

项目主库是 MySQL，但 PgVector 只支持 PostgreSQL。方案：新增一个 PostgreSQL 实例专门存向量数据（rag_document + rag_chunk 两张表），业务数据继续用 MySQL。两个数据源通过 Spring 多数据源配置管理。LangChain4j 官方只实现了 PgVector 的 `EmbeddingStore`，没有 MySQL 的。

### 为什么不引入独立向量数据库（如 Milvus）

两张表、几十条文档的规模，PgVector 完全够用。独立向量数据库需要额外部署和运维，对毕设项目是过度设计。

## 2. 系统架构

```
用户端（Vue 3）
  ├─ 我的体检详情 ──→ GET  /api/records/{id}
  │                   GET  /api/rag/health-advice?recordId={id}
  │
  └─ 评估结果页 ────→ POST /api/rag/suggest-plan
                       请求：{ recordId, riskLevel, hitRules }

后端（Spring Boot）
  ├─ RagController
  │   ├─ healthAdvice(recordId)     → RagService
  │   └─ suggestPlan(dto)            → RagService
  │
  ├─ RagService
  │   ├─ 收集异常指标 / 风险信息
  │   ├─ Embedding → PgVector 检索 top 3 知识块
  │   ├─ 拼 prompt → DeepSeek Chat
  │   └─ 返回回答 + 来源引用
  │
  └─ PgVector (PostgreSQL)
      └─ rag_chunk 表（向量 + 文本）
```

## 3. API 设计

### 3.1 获取 AI 健康建议

```
GET /api/rag/health-advice?recordId={recordId}

响应：
{
  "advice": "根据您的体检结果，以下指标需要关注：\n\n1. 收缩压 148mmHg（偏高）...",
  "sources": [
    { "docName": "高血压防治指南", "chunkText": "血压偏高人群应控制..." }
  ]
}
```

**后端逻辑**：
1. 根据 recordId 查 exam_result_item 表，找出所有 is_abnormal != 0 的指标
2. 拼成描述："用户收缩压148，舒张压95，甘油三酯2.3..."
3. 用这个描述去 PgVector 检索相关知识
4. DeepSeek 生成建议

### 3.2 获取 AI 干预建议

```
POST /api/rag/suggest-plan
请求：
{
  "userId": 5,
  "recordId": 3002,
  "riskLevel": "高",
  "totalScore": 75.5,
  "hitRules": ["收缩压中度偏高", "BMI肥胖", "血糖偏高"]
}

响应：
{
  "suggestion": "建议为该员工制定减重管理计划：\n1. 目标：12周内BMI降至24以下...",
  "suggestedType": "weight",
  "suggestedTitle": "个人减重干预计划",
  "sources": [...]
}
```

**后端逻辑**：
1. 拿风险等级 + 命中规则名作为检索关键词
2. PgVector 检索干预方案相关知识
3. DeepSeek 生成具体干预步骤
4. 同时分析应使用哪种干预类型（weight/smoking/exercise 等）

## 4. 前端改动

### 4.1 员工端体检详情（embed: Exams.vue）

在报告详情页底部新增一个卡片：

```
┌─────────────────────────────────┐
│ 体检指标详情                      │
│ 收缩压 148  异常  舒张压 95  异常  │
│ ...                             │
├─────────────────────────────────┤
│ 🤖 AI 健康建议          [刷新]   │
│                                 │
│ 根据您的体检结果...               │
│                                 │
│ 来源：高血压防治指南               │
└─────────────────────────────────┘
```

页面进入时自动调用 `/api/rag/health-advice`，`v-loading` 显示加载态。

### 4.2 管理端评估结果（embed: Results.vue）

在操作列新增一个按钮"AI 建议"：

```
点击"AI 建议" → 弹窗展示建议内容 + 引用来源
弹窗底部按钮：["转为干预计划"]
点击"转为干预计划" → 跳转到创建计划页面，标题和描述预填
```

## 5. 包结构

```
com.enterprise.health
├── rag/
│   ├── RagController.java        # API 接口（2 个方法）
│   ├── RagService.java           # 检索 + 生成逻辑
│   ├── DocumentLoader.java       # 启动时加载文档
│   ├── HealthAdviceService.java  # 体检建议生成
│   └── config/
│       └── RagConfig.java        # LangChain4j Bean + DeepSeek 配置
```

## 6. 知识库文档来源

初始内置 5 篇健康知识文档（放在 `docs/rag/` 目录）：

| 文档 | 内容 |
|------|------|
| 体检指标解读.md | 血压、血糖、血脂等常见指标的正常范围和异常说明 |
| 高血压防治指南.md | 饮食、运动、用药建议 |
| 糖尿病预防指南.md | 血糖管理、饮食控制 |
| 企业健康干预方案.md | 减重、戒烟、心理辅导等干预模板 |
| 健康饮食建议.md | 日常营养搭配建议 |
