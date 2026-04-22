package com.example.components;

import jakarta.jms.JMSException;
import jakarta.jms.Message;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

/**
 * 死信队列监听器
 */
@Slf4j
//@Component
public class DLQListener {

    @JmsListener(destination = "ActiveMQ.DLQ")
    public void onDeadLetterMessage(String payload, Message jmsMessage) {
        try {
            log.warn(
                    "[DLQ] 收到死信 payload={} messageId={} redelivered={}",
                    payload,
                    jmsMessage.getJMSMessageID(),
                    jmsMessage.getJMSRedelivered()
            );
        } catch (JMSException e) {
            log.error("[DLQ] 读取 JMS 头失败", e);
        }
    }
}
