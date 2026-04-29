package com.example.services;

import com.example.config.IdempotencyProperties;
import com.example.config.ReplayProperties;
import com.example.mappers.IdempotencyRecordMapper;
import com.example.pojos.entitis.IdempotencyRecord;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;

/**
 * 幂等服务
 */
@Service
@RequiredArgsConstructor
public class IdempotencyService {

    private final StringRedisTemplate redisTemplate;
    private final IdempotencyProperties properties;
    private final IdempotencyRecordMapper idempotencyRecordMapper;
    private final ReplayProperties replayProperties;

    /**
     * 标记是否为首次请求
     * @param idempotencyKey 幂等键值
     * @return 是否为首次请求
     */
    public boolean markIfFirstTime(String idempotencyKey) {
        String key = properties.getRedisKeyPrefix() + idempotencyKey;
        Duration ttl = Duration.ofSeconds(properties.getTtlSeconds());
        Boolean ok = redisTemplate.opsForValue().setIfAbsent(key, "1", ttl);
        return Boolean.TRUE.equals(ok);
    }

    /**
     * 保存审核记录
     * @param idempotencyKey 幂等键值
     * @param payload 审核记录
     */
    public void saveAuditRecord(String idempotencyKey, String payload) {
        IdempotencyRecord record = new IdempotencyRecord();
        record.setIdempotencyKey(idempotencyKey);
        record.setPayload(payload);
        idempotencyRecordMapper.insert(record);
    }

    /**
     * 保存重放审核记录
     */
    public Integer saveReplayAuditRecord(String replayKey, String sourceKey, String payload, String operator, String reason) {
        Integer maxReplayNo = idempotencyRecordMapper.selectMaxReplayNoBySourceKey(sourceKey);
        int nextReplayNo = (maxReplayNo == null ? 0 : maxReplayNo) + 1;

        IdempotencyRecord record = new IdempotencyRecord();
        record.setIdempotencyKey(replayKey);
        record.setSourceKey(sourceKey);
        record.setPayload(payload);
        record.setStatus("REPLAYED");
        record.setReplayNo(nextReplayNo);
        record.setReplayOperator(operator);
        record.setReplayReason(reason);
        idempotencyRecordMapper.insert(record);

        return nextReplayNo;
    }

    /**
     * 获取最大回放次数
     */
    public Integer getMaxReplayTimes() {
        return replayProperties.getMaxTimes() == null ? 3 : replayProperties.getMaxTimes();
    }

    /**
     * 获取重放审核记录
     */
    public List<IdempotencyRecord> getReplayHistory(String sourceKey) {
        return idempotencyRecordMapper.selectReplayHistoryBySourceKey(sourceKey);
    }

    /**
     * 判断是否可以重放
     */
    public boolean canReplay(String sourceKey) {
        Integer maxReplayNo = idempotencyRecordMapper.selectMaxReplayNoBySourceKey(sourceKey);
        int current = (maxReplayNo == null ? 0 : maxReplayNo);
        int limit = (replayProperties.getMaxTimes() == null ? 3 : replayProperties.getMaxTimes());
        return current < limit;
    }
}
