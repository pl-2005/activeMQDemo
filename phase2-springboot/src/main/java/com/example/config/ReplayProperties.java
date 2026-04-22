package com.example.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "demo.replay")
public class ReplayProperties {
    /**
     * 单个 sourceKey 最大回放次数
     */
    private Integer maxTimes = 3;
}
