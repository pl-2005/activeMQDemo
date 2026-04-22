package com.example.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 幂等配置属性
 */
@Data
@ConfigurationProperties(prefix = "demo.idempotency")
public class IdempotencyProperties {

    /**
     * Redis 中幂等键前缀
     */
    private String redisKeyPrefix;
    /**
     * 幂等记录保留时间（秒）
     */
    private long ttlSeconds;
}
