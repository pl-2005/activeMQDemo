# 消息回放（Phase 6-3）

本页目标：把“死信/失败消息怎么安全重放”讲成可执行流程。  
约束：基于当前项目现状（Redis 幂等 + ActiveMQ + MySQL 审计），**先文档化流程，不强推改代码**。

---

## 1. 回放是什么，不是什么

- **是什么**：在定位并修复问题后，把失败消息重新送回业务队列，让消费者再处理一次。
- **不是什么**：盲目“再发一次”。如果未做幂等与风险闸门，回放会放大事故。

---

## 2. 当前项目中的回放入口

你当前可用的入口有两类：

1. **从业务接口重放（当前最易执行）**
   - 重新调用 `POST /api/message`。
   - 重点是决定是否沿用原 `X-Idempotency-Key`（通常不能直接沿用，见下一节）。

2. **从 DLQ/审计记录人工回放（企业更常见）**
   - 在控制台 `Browse` 死信，拿到 payload 与关键标识。
   - 人工评估后，通过受控接口/工具再次发送到业务队列。

---

## 3. 回放前的三道闸门（必须）

### 闸门 A：先修根因

不修根因就回放，只会再次进 DLQ。  
例如：消费代码 bug、下游依赖不可用、脏数据未修复。

### 闸门 B：确认幂等策略

当前接口入口使用 `X-Idempotency-Key` 做 Redis `SET NX`。

- 若使用**原 key**回放：大概率会被 Redis 直接拦截（返回“重复请求，已忽略”）。
- 若使用**新 key**回放：能发送成功，但要确保业务侧不会造成二次副作用。

建议：回放 key 使用可追溯格式（示例）

`原始key + ":replay:" + 时间戳`

这样可在日志/审计中追踪“这是第几次回放”。

### 闸门 C：目标环境与队列确认

结合 `docs/multi-environment.md`：

- 先确认当前 `spring.profiles.active`
- 再确认目标队列 `demo.jms.queue-name`
- 禁止在不确认环境的情况下回放（避免把测试消息打到生产）

---

## 4. 推荐回放流程（当前项目可直接执行）

1. 在 ActiveMQ 控制台 `Browse` DLQ，记录：
   - payload
   - 原消息时间
   - 关联业务号（若有）
2. 确认根因已修复（代码、配置、数据至少一项已纠正）。
3. 生成回放幂等键（例如 `KEY-2-2:replay:20260421T210500`）。
4. 调用 `POST /api/message` 发回业务队列。
5. 观察：
   - 生产者日志发送成功
   - 消费者日志处理成功（不再抛异常）
   - 必要时查审计表记录（新增一条 replay 请求记录）

---

## 5. 失败场景与处理建议

| 场景 | 建议 |
|------|------|
| 回放后再次失败进入 DLQ | 立即停止批量回放，重新定位根因 |
| 回放被“重复请求”拦截 | 检查是否误用了原 `X-Idempotency-Key` |
| 回放成功但业务侧出现重复影响 | 回放前缺少幂等保护，需补业务幂等键/去重策略 |
| 不确定该不该回放 | 默认不自动回放，转人工审批 |

---

## 6. 当前 Demo 的最小验收

### 验收目标

- 能说清“为什么原 key 回放会被拦截”
- 能用新 replay key 完成一次受控重放
- 能证明消息被消费者正常处理（不再进 DLQ）

### 建议命令（PowerShell）

先准备一条失败样本（示意）：

```powershell
curl.exe -X POST "http://localhost:8080/api/message" -H "Content-Type: application/json" -H "X-Idempotency-Key: KEY-REPLAY-1" -d "{\"text\":\"FAIL_ME\"}"
```

根因修复后，用 replay key 发送正常载荷（示意）：

```powershell
curl.exe -X POST "http://localhost:8080/api/message" -H "Content-Type: application/json" -H "X-Idempotency-Key: KEY-REPLAY-1:replay:20260421T210500" -d "{\"text\":\"hello replay\"}"
```

---

## 7. 进阶（后续可做，不是本步必做）

1. 新增“回放专用接口”，强制记录：
   - replayOperator
   - replayReason
   - sourceMessageId
2. 审计表增加 `source_key` 与 `replay_no` 字段。
3. 增加“回放审批 + 限流 + 熔断”，避免批量误操作。

