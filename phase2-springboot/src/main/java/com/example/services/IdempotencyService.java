package com.example.services;

import com.example.config.IdempotencyProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class IdempotencyService {

    private final StringRedisTemplate redisTemplate;
    private final IdempotencyProperties properties;

    public boolean markIfFirstTime(String idempotencyKey) {
        String key = properties.getRedisKeyPrefix() + idempotencyKey;
        Duration ttl = Duration.ofSeconds(properties.getTtlSeconds());
        Boolean ok = redisTemplate.opsForValue().setIfAbsent(key, "1", ttl);
        return Boolean.TRUE.equals(ok);
    }
}
