package com.example.controllers;

import com.example.pojos.dto.MessageSendDto;
import com.example.pojos.vo.ApiResponseVo;
import com.example.services.IdempotencyService;
import com.example.services.MessageProducerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/message")
@RequiredArgsConstructor
public class MessageController {

    private final MessageProducerService messageProducerService;
    private final IdempotencyService idempotencyService;

    @PostMapping
    public ResponseEntity<ApiResponseVo<Map<String, Object>>> send(
            @RequestHeader("X-Idempotency-Key") String idempotencyKey,
            @Valid @RequestBody MessageSendDto request
    ) {
        boolean firstTime = idempotencyService.markIfFirstTime(idempotencyKey);

        if (!firstTime) {
            return ResponseEntity.ok(new ApiResponseVo<>(
                    "200",
                    "重复请求，已忽略",
                    Map.of(
                            "queue", "demo.queue.boot",
                            "text", request.getText(),
                            "idempotencyKey", idempotencyKey,
                            "firstTime", false
                    )
            ));
        }

        messageProducerService.sendMessage(request.getText());

        return ResponseEntity.ok(new ApiResponseVo<>(
                "200",
                "消息已发送",
                Map.of(
                        "queue", "demo.queue.boot",
                        "text", request.getText(),
                        "idempotencyKey", idempotencyKey,
                        "firstTime", true
                )
        ));
    }
}
