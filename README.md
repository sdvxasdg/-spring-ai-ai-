# 智能招聘 Agent 系统

基于 Spring AI + DashScope 的智能招聘助手，支持简历上传、RAG 问答、工具调用、流式对话。

## 技术栈

- Java 17 + Spring Boot 3.2 + Spring AI 1.1.2
- Spring AI Alibaba、DashScope（qwen-plus）
- MySQL + MyBatis-Plus、PostgreSQL + PgVector、Redis
- Knife4j、Docker Compose

## 核心功能

- **简历上传**：PDF / Word / TXT 解析，向量化入库
- **RAG 问答**：检索简历内容，结果注入 Prompt，带来源引用
- **Tool Calling**：岗位查询、投递记录、候选人筛选、面试安排 4 个工具
- **多轮对话**：Redis List 持久化历史，支持多会话隔离
- **流式输出**：SSE 事件流（message / done / error / ping）

## 架构
客户端 → Controller → Service/Tool → Spring AI → 外部资源
├── ChatClient（大模型）
├── VectorStore（RAG）
└── Tools（工具调用）
外部资源：DashScope + MySQL + PgVector + Redis

## 技术亮点
RAG + Tool Calling 混合决策：模型自主判断用知识库还是调工具

Redis List + Lua 原子写：解决多轮对话并发写入问题

PgVector 持久化：重启数据不丢

统一响应 Result<T> + 全局异常 + Knife4j 文档
