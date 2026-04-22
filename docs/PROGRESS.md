# 学习进度（可断点续学）

中断后请先读根目录 `README.md` 与本文件，再从「当前下一步」继续。

## 已完成（摘要）

- Phase 1：原生 JMS（Queue / Topic / Ack）
- Phase 2：Spring Boot + `JmsTemplate` + `@JmsListener`
- Phase 3：Redis 幂等（R1～R5）
- Phase 4：MySQL + MyBatis-Flex 幂等审计（M1～M5）
- Phase 5：DLQ 概念与控制台（见 [DLQ.md](./DLQ.md)，是否做完 D1～D4 请自行勾选 README）

## Phase 6：企业级补充（约定顺序）

### 6-1 多环境配置（已完成工程化拆分）

见 [multi-environment.md](./multi-environment.md)。要点：`application-dev.yml` / `application-prod.yml` / 可选 `application-local.yml`。

### 6-2 与 MySQL 审计的一致性说明（已完成）

见 [mysql-audit-consistency.md](./mysql-audit-consistency.md)。核心：当前是 **最终一致 + 可补偿**，不是单事务强一致。

### 6-3 消息回放（已完成文档首版）

见 [message-replay.md](./message-replay.md)。覆盖回放闸门、操作流程、风险与最小验收。

---

若进入下一轮深化（如“回放专用接口”或“审计状态机”）可发送：

`[开始下一步]`

若直接进入 **6-3**，也可在同一消息里写清。
