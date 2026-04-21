package com.example.services;

import com.example.config.IdempotencyProperties;
import com.example.mappers.IdempotencyRecordMapper;
import com.example.pojos.entitis.IdempotencyRecord;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class IdempotencyService {

    private final StringRedisTemplate redisTemplate;
    private final IdempotencyProperties properties;
    private final IdempotencyRecordMapper idempotencyRecordMapper;

    public boolean markIfFirstTime(String idempotencyKey) {
        String key = properties.getRedisKeyPrefix() + idempotencyKey;
        Duration ttl = Duration.ofSeconds(properties.getTtlSeconds());
        Boolean ok = redisTemplate.opsForValue().setIfAbsent(key, "1", ttl);
        return Boolean.TRUE.equals(ok);
    }

    public void saveAuditRecord(String idempotencyKey, String payload) {
        IdempotencyRecord record = new IdempotencyRecord();
        record.setIdempotencyKey(idempotencyKey);
        record.setPayload(payload);
        idempotencyRecordMapper.insert(record);
    }
}
