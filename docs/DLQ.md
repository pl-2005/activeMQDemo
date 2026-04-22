# DLQ / 死信（ActiveMQ Classic）

本页配合根目录 `README.md` 中 **Phase 5（DLQ）** 使用：先理解概念，再用手写代码与控制台完成验收。

---

## 1. 你要搞清的三件事

1. **重投（Redelivery）**  
   消费者在 **未确认** 消费成功前失败（例如业务抛异常、事务回滚、`CLIENT_ACK` 下未 `acknowledge`），Broker 会把同一条消息再次投递。日志里可看到 `JMSRedelivered=true`、`JMSXDeliveryCount` 递增。

2. **Poison ACK**  
   当 **重投次数** 超过连接上配置的 **`RedeliveryPolicy.maximumRedeliveries`** 时，客户端会告诉 Broker：这条消息是“毒消息”，不要再重试了。Broker 随后把它转入 **死信队列（DLQ）**。

3. **默认 DLQ 名称**  
   ActiveMQ Classic 默认使用队列 **`ActiveMQ.DLQ`** 存放无法投递成功的消息（也可用 Broker 的 `individualDeadLetterStrategy` 改成按业务队列分 DLQ，例如 `DLQ.demo.queue.boot`）。官方说明见 [Message Redelivery and DLQ Handling](https://activemq.apache.org/components/classic/documentation/message-redelivery-and-dlq-handling)。

---

## 2. 和本 Demo 的对应关系

| 项目内已有内容 | 作用 |
|----------------|------|
| `MessageConsumerListener` 对正文 `FAIL_ME` 抛异常 | 模拟业务失败，触发重投 |
| 日志中的「是否重投」「投递次数」 | 观察重投是否发生 |
| `JmsErrorHandlerConfig` | 仅记录异常；**不要**在 ErrorHandler 里“吞掉”异常并假装成功，否则会干扰你对 ack/重投的理解（保持当前“只打日志”即可） |

**注意**：Broker 对 **非持久化** 消息的 DLQ 策略默认可能不同；`JmsTemplate` 默认多为持久化投递，本 Demo 一般无需改发送端。若你改成非持久化，请阅读官方文档中的 `processNonPersistent`。

---

## 3. 实施步骤与验收

### D1 — 先在不改代码的情况下观察重投

1. 启动 ActiveMQ（Docker 或本地）与 `com.example.Application`。
2. 使用新的幂等键发送正文 **`FAIL_ME`**（`POST /api/message`）。
3. 在应用日志中确认：**重投为 true** 且 **`JMSXDeliveryCount` 递增**。

**验收**：能看到多次失败重试日志；此时消息可能尚未进 DLQ（默认 `maximumRedeliveries` 多为 **6**，总尝试次数较多）。

---

### D2 — 缩短进入 DLQ 的路径（手写代码）

目标：把 **`maximumRedeliveries`** 调小（例如 **2**），使消息更快被判定为毒消息并进入 **`ActiveMQ.DLQ`**。

在 `phase2-springboot` 中 **新建** 配置类（类名可自定），使用 Spring Boot 提供的 **`ActiveMQConnectionFactoryCustomizer`**，对 **`ActiveMQConnectionFactory`** 设置 **`RedeliveryPolicy`**：

- `setMaximumRedeliveries(2)`（数值可按教学需要调整）
- 可选：`setInitialRedeliveryDelay`、`setRedeliveryDelay` 调小，方便你快速在控制台看到 DLQ（避免长时间等待）

**手写参考（请按需调整类名与数值，并确保与包路径一致）**：

```java
package com.example.config;

import org.apache.activemq.RedeliveryPolicy;
import org.springframework.boot.autoconfigure.jms.activemq.ActiveMQConnectionFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ActiveMqRedeliveryConfig {

    @Bean
    public ActiveMQConnectionFactoryCustomizer redeliveryPolicyCustomizer() {
        return factory -> {
            RedeliveryPolicy policy = new RedeliveryPolicy();
            policy.setMaximumRedeliveries(2);
            policy.setInitialRedeliveryDelay(300);
            policy.setRedeliveryDelay(300);
            factory.setRedeliveryPolicy(policy);
        };
    }
}
```

**提示**：

- 若编译器找不到 `ActiveMQConnectionFactoryCustomizer`，请确认 `spring-boot-starter-activemq` 已引入，且 IDE 已刷新 Maven。
- `maximumRedeliveries` 与“第几次日志算一次投递”的对应关系，以 Broker + 客户端组合为准；教学上只要能在 **合理次数内** 在 `ActiveMQ.DLQ` 看到消息即可。

**验收**：应用启动无报错；再次发送 `FAIL_ME` 后，经过有限次重试，**业务队列上该消息应不再堆积**（已进入 DLQ）。

---

### D3 — 在 Web 控制台确认 DLQ

1. 打开 `http://localhost:8161`（账号密码以你环境为准，常见 `admin/admin`）。
2. 进入 **Queues**，查找 **`ActiveMQ.DLQ`**（若 Broker 配置了 `individualDeadLetterStrategy`，则可能是 **`DLQ.demo.queue.boot`** 等名称）。
3. 确认 **Messages Enqueued** 或待消费条数 **≥ 1**，且消息体或自定义属性能对应你发送的 `FAIL_ME`。

**验收**：能在控制台看到死信队列中的消息。

---

### D4（可选）— 消费或清理 DLQ

任选其一作为练习：

- **运维向**：在控制台手动 **Browse** 死信、记录 Message ID、必要时 **Delete** 或移到修复队列。
- **开发向**：新增一个 **`@JmsListener(destination = "ActiveMQ.DLQ")`**（或你的 `DLQ.xxx` 名），只打日志、不要做危险自动重投，避免把毒消息无限打回业务队列。

**验收**：能说明“DLQ 中的消息为什么进来、上线后通常由谁处理”。

---

## 4. 常见问题

| 现象 | 可能原因 |
|------|----------|
| 始终没有 DLQ，只有无限重试 | `maximumRedeliveries` 过大或未生效；连接未走你自定义的 `ConnectionFactory` |
| 控制台看不到 `ActiveMQ.DLQ` | 尚未产生死信，或 Broker 使用了自定义 deadLetter 前缀 |
| 重投次数不增加 | 监听器实际已 ack（例如异常被吞、或确认模式与预期不符）；需对照 Spring JMS 文档排查 |

---

## 5. 参考

- [Message Redelivery and DLQ Handling](https://activemq.apache.org/components/classic/documentation/message-redelivery-and-dlq-handling)（Apache ActiveMQ Classic）
- [Redelivery Policy](https://activemq.apache.org/components/classic/documentation/redelivery-policy)
