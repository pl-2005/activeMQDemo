package com.example.services;

import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 学习版：进程内幂等。后续 Phase3 将替换为 Redis SET NX。
 */
@Service
public class IdempotencyService {

    private final ConcurrentMap<String, Boolean> processedKeys = new ConcurrentHashMap<>();

    /**
     * @return true 首次；false 重复
     */
    public boolean markIfFirstTime(String key) {
        return processedKeys.putIfAbsent(key, Boolean.TRUE) == null;
    }
}
