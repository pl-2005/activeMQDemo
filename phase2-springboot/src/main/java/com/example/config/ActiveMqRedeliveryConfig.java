package com.example.config;

import org.apache.activemq.RedeliveryPolicy;
import org.springframework.boot.autoconfigure.jms.activemq.ActiveMQConnectionFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 消息重发策略配置
 */
@Configuration

public class ActiveMqRedeliveryConfig {

    @Bean
    public ActiveMQConnectionFactoryCustomizer redeliveryPolicyCustomizer() {
        return factory -> {
            RedeliveryPolicy policy = new RedeliveryPolicy();
            policy.setMaximumRedeliveries(2);
            policy.setMaximumRedeliveries(2);
            policy.setInitialRedeliveryDelay(300);
            policy.setRedeliveryDelay(300);
            factory.setRedeliveryPolicy(policy);
        };
    }
}
