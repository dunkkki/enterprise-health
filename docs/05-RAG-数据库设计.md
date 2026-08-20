# AI 智能健康建议 — 数据库设计

## 新增表（2 张）

### rag_document 知识文档表

| 字段 | 类型 | 说明 |
|------|------|------|
| id | varchar(64) | PK，文档唯一标识 |
| file_name | varchar(256) | 原始文件名 |
| file_type | varchar(16) | 文件类型：PDF / TXT / MD |
| status | tinyint | 0=处理中 1=已上线 2=已下线 |
| chunk_count | int | 切片数量 |
| created_at | datetime | |

### rag_chunk 文档切片表

| 字段 | 类型 | 说明 |
|------|------|------|
| id | varchar(64) | PK |
| document_id | varchar(64) | FK → rag_document.id |
| chunk_text | text | 切片文本内容（300-500字） |
| chunk_index | int | 切片序号 |
| embedding | vector(1536) | DeepSeek 生成的文本向量 |
| created_at | datetime | |

## 索引

```sql
-- 向量索引：按余弦距离检索最相似的 N 个切片
CREATE INDEX idx_rag_chunk_embedding ON rag_chunk
  USING ivfflat (embedding vector_cosine_ops) WITH (lists = 100);
```

## ER 关系

```
rag_document 1 ──── N rag_chunk
```

## 建表 SQL

```sql
CREATE EXTENSION IF NOT EXISTS vector;

CREATE TABLE rag_document (
    id VARCHAR(64) PRIMARY KEY,
    file_name VARCHAR(256) NOT NULL,
    file_type VARCHAR(16) NOT NULL,
    status TINYINT DEFAULT 0,
    chunk_count INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE rag_chunk (
    id VARCHAR(64) PRIMARY KEY,
    document_id VARCHAR(64) NOT NULL REFERENCES rag_document(id) ON DELETE CASCADE,
    chunk_text TEXT NOT NULL,
    chunk_index INT NOT NULL,
    embedding vector(1536),
    created_at TIMESTAMP DEFAULT NOW()
);

CREATE INDEX idx_rag_chunk_embedding ON rag_chunk
  USING ivfflat (embedding vector_cosine_ops) WITH (lists = 100);
```
