package com.example.components;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MessageConsumerListener {

    @JmsListener(destination = "${demo.jms.queue-name}")
    public void receiveMessage(String message) {
        log.info("从队列接收消息: {}", message);
    }
}
