package com.example.components;

import jakarta.jms.JMSException;
import jakarta.jms.Message;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MessageConsumerListener {

    @JmsListener(destination = "${demo.jms.queue-name}")
    public void receiveMessage(String payload, Message jmsMessage) {
        long start = System.currentTimeMillis();
        int deliveryCount = getDeliveryCount(jmsMessage);
        boolean redelivered = getRedelivered(jmsMessage);

        log.info("消费者开始处理 queue={} payload={} 是否重投={} 投递次数={}",
                "demo.queue.boot", payload, redelivered, deliveryCount);

        try {
            if ("FAIL_ME".equals(payload)) {
                throw new IllegalStateException("模拟消费业务异常");
            }

            long cost = System.currentTimeMillis() - start;
            log.info("消费者处理成功 queue={} payload={} 耗时毫秒={} 是否重投={} 投递次数={}",
                    "demo.queue.boot", payload, cost, redelivered, deliveryCount);
        } catch (Exception e) {
            long cost = System.currentTimeMillis() - start;
            log.error("消费者处理失败 queue={} payload={} 耗时毫秒={} 是否重投={} 投递次数={} 原因={}",
                    "demo.queue.boot", payload, cost, redelivered, deliveryCount, e.getMessage(), e);

            int maxRetryWarnThreshold = 5;
            if (deliveryCount >= maxRetryWarnThreshold) {
                log.warn("消费者重试次数超过阈值 queue={} payload={} 投递次数={} 提示=消息即将进入死信队列(DLQ)",
                        "demo.queue.boot", payload, deliveryCount);
            }
            throw e;
        }
    }

    private int getDeliveryCount(Message message) {
        try {
            Object value = message.getObjectProperty("JMSXDeliveryCount");
            if (value instanceof Number number) {
                return number.intValue();
            }
            return -1;
        } catch (JMSException e) {
            log.warn("读取 JMSXDeliveryCount 失败: {}", e.getMessage());
            return -1;
        }
    }

    private boolean getRedelivered(Message message) {
        try {
            return message.getJMSRedelivered();
        } catch (JMSException e) {
            log.warn("读取 JMSRedelivered 失败: {}", e.getMessage());
            return false;
        }
    }
}
