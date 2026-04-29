# ActiveMQ Demo

一个基于 Java 的消息中间件实践项目，覆盖从原生 JMS 到 Spring Boot 企业化演进的完整路径。  
项目已完成核心闭环：消息发送/消费、幂等控制、审计落库、DLQ 治理、消息回放、多环境配置。

## 核心特性

- **双模块教学与工程并行**
  - `phase1-jms`：原生 JMS（Queue / Topic / Ack）基础实验
  - `phase2-springboot`：Spring Boot 实战化实现
- **接口幂等**
  - `X-Idempotency-Key` + Redis `SET NX EX`
- **审计与追踪**
  - MySQL `idempotency_record` 留痕
- **失败治理**
  - 消费异常重试与 DLQ（`ActiveMQ.DLQ`）观测
- **受控回放**
  - 回放接口、回放历史查询、最大回放次数限制
- **多环境配置**
  - `dev / prod / local` profile 拆分

## 技术栈

- Java 21
- Spring Boot 3
- ActiveMQ Classic
- Redis
- MySQL
- MyBatis-Flex
- Maven

## 项目结构

```text
activeMQDemo
├─ README.md
├─ docs
│  ├─ DLQ.md
│  ├─ message-replay.md
│  ├─ mysql-audit-consistency.md
│  ├─ multi-environment.md
│  └─ PROGRESS.md
├─ phase1-jms
└─ phase2-springboot
```

## 快速开始

### 1) 启动依赖服务

推荐使用 Docker 启动 ActiveMQ：

```bash
docker run -d --name activemq -p 61616:61616 -p 8161:8161 rmohr/activemq
```

- Broker: `tcp://localhost:61616`
- Console: `http://localhost:8161`（常见默认账号 `admin/admin`）

Redis / MySQL 可按本地环境自备，默认配置见 `application-dev.yml`。

### 2) 构建项目

```bash
mvn clean package -DskipTests
```

### 3) 运行 Spring Boot 模块

启动类：

- `phase2-springboot/src/main/java/com/example/Application.java`

默认 profile 为 `dev`。  
生产环境建议显式指定：

```bash
--spring.profiles.active=prod
```

## API 概览（phase2-springboot）

- `POST /api/message`
  - 普通消息发送（幂等控制）
- `POST /api/message/replay`
  - 受控回放
- `GET /api/message/replay/history?sourceKey=...`
  - 查询回放历史

响应统一结构：

- `code`
- `message`
- `data`

## 已完成能力（项目状态）

- [x] 原生 JMS：Queue / Topic / AUTO_ACK / CLIENT_ACK
- [x] Spring Boot JMS：`JmsTemplate` + `@JmsListener`
- [x] Redis 幂等（接口防重）
- [x] MySQL 审计留痕
- [x] DLQ 重试与死信治理
- [x] 多环境配置（dev / prod / local）
- [x] 消息回放与回放历史
- [x] 回放次数限流（`demo.replay.max-times`）
- [x] 业务码与响应结构标准化

## 配置说明

关键配置文件（`phase2-springboot/src/main/resources`）：

- `application.yml`：公共配置与默认 profile
- `application-dev.yml`：本地开发配置
- `application-prod.yml`：生产占位配置（环境变量注入）
- `application-local.yml.example`：本地私有覆盖模板

## 验收建议

建议按以下顺序验证：

1. 正常发送消息成功
2. 同幂等键重复发送被拦截
3. 消费失败触发重试并进入 DLQ
4. 回放成功并写入回放审计
5. 超过回放阈值后被拒绝
6. 回放历史查询返回正确记录

## 文档导航

- [docs/PROGRESS.md](docs/PROGRESS.md)：阶段进度
- [docs/DLQ.md](docs/DLQ.md)：重试与死信治理
- [docs/multi-environment.md](docs/multi-environment.md)：多环境配置
- [docs/mysql-audit-consistency.md](docs/mysql-audit-consistency.md)：一致性边界与补偿
- [docs/message-replay.md](docs/message-replay.md)：回放流程与风控

## 后续可扩展方向

- 审计状态机（`INIT/SENT/CONSUMED/FAILED/DLQ/REPLAYED`）
- DLQ 告警与回放审批
- Outbox / 事务消息提升一致性

