package com.example.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.ErrorHandler;


@Slf4j
@Configuration
public class JmsErrorHandler {

    @Bean
    public ErrorHandler jmsErrorHandler() {
        return t -> {
            log.error("jms异常处理器捕获到异常：{}", t.getMessage());
        };
    }
}
