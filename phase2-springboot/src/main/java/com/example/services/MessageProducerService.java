package com.example.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageProducerService {
    private final JmsTemplate jmsTemplate;

    @Value("${demo.jms.queue-name}")
    private String queueName;

    public void sendMessage(String message) {
        jmsTemplate.convertAndSend(queueName, message);
        log.info("向队列发送消息: {}: {}", queueName, message);
    }
}
