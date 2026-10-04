# LangChain4j Enterprise Knowledge Agent

这是一个基于 Spring Boot、LangChain4j、MyBatis-Plus 和 Redis 的企业知识库客服项目，当前完成到第 6 课。

## 当前进度

- AI Service：通过 `@AiService` 声明企业知识库客服。
- 结构化输出：返回 `SupportResponse`。
- Chat Memory：使用 `@MemoryId` 区分会话。
- Redis Memory：使用 `ChatMemoryStore` 保存会话消息。
- 文档管理：使用 MyBatis-Plus 管理租户、上传者、版本和处理状态。
- 文档解析与切分：使用 LangChain4j 将 UTF-8 文本解析为 `Document`，再切分为 `TextSegment`。

当前还没有接入 Embedding、向量数据库和在线 RAG 检索。

## 环境要求

- Java 17
- Maven 3.9+
- Docker / WSL
- 智谱 API Key（真实模型请求时）

## 安全配置

不要把真实密钥、数据库密码或个人配置提交到 Git。

PowerShell 示例：

```powershell
$env:API_KEY_GLM="your-zhipu-api-key"
$env:MYSQL_USERNAME="consultant"
$env:MYSQL_PASSWORD="your-local-mysql-password"
$env:MYSQL_ROOT_PASSWORD="your-local-mysql-root-password"
```

项目配置只从环境变量读取：

- `API_KEY_GLM`（兼容旧变量名 `API-KEY-GLM`）
- `MYSQL_USERNAME`
- `MYSQL_PASSWORD`
- `MYSQL_ROOT_PASSWORD`
- `REDIS_HOST`
- `REDIS_PORT`

在 IntelliJ 中运行时，也可以把这些变量配置到 `ConsultantApplication` 的 Run Configuration。

## 测试

```powershell
$env:JAVA_HOME="D:\Program Files\Java\jdk-17"
mvn clean test -q
```

## Docker MySQL

在 WSL 中执行：

```bash
export MYSQL_USERNAME=consultant
export MYSQL_PASSWORD='replace-with-local-password'
export MYSQL_ROOT_PASSWORD='replace-with-local-root-password'
docker compose -f compose.mysql.yaml up -d
```

MySQL 初始化脚本位于：

```text
src/main/resources/db/mysql/01-create-knowledge-document.sql
```

## 当前接口

```text
GET /chat?conversationId=session-1&message=你好
```

文档管理接口：

```text
POST /api/knowledge/documents
GET  /api/knowledge/documents?tenantId=company-a
POST /api/knowledge/documents/{id}/processing
POST /api/knowledge/documents/{id}/publish
POST /api/knowledge/documents/{id}/fail?message=...
POST /api/knowledge/documents/{id}/archive
```
