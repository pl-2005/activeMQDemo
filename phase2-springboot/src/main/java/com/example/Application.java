package com.example;

import com.example.config.IdempotencyProperties;
import com.example.config.ReplayProperties;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({IdempotencyProperties.class, ReplayProperties.class})
@MapperScan("com.example.mappers")
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
