package com.example.controllers;

import com.example.pojos.dto.MessageSendDto;
import com.example.pojos.dto.ReplayRequestDto;
import com.example.pojos.entitis.IdempotencyRecord;
import com.example.pojos.vo.ApiResponseVo;
import com.example.services.IdempotencyService;
import com.example.services.MessageProducerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 消息控制器
 * 提供消息发送接口
 */
@RestController
@RequestMapping("/api/message")
@RequiredArgsConstructor
public class MessageController {

    private final MessageProducerService messageProducerService;
    private final IdempotencyService idempotencyService;

    /**
     * 发送消息
     * @param idempotencyKey 幂等键，用于判断是否重复请求
     * @param request 消息发送请求
     * @return 发送结果
     */
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
        idempotencyService.saveAuditRecord(idempotencyKey, request.getText());

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

    /**
     * 回放消息
     * @param request 回放请求参数
     * @return 回放结果参数
     */
    @PostMapping("/replay")
    public ResponseEntity<ApiResponseVo<Map<String, Object>>> replay(@Valid @RequestBody ReplayRequestDto request) {
        if (!idempotencyService.canReplay(request.getSourceKey())) {
            return ResponseEntity.ok(new ApiResponseVo<>(
                    "409",
                    "超过最大回放次数，已拒绝",
                    Map.of(
                            "sourceKey", request.getSourceKey(),
                            "maxTimes", idempotencyService.getMaxReplayTimes(request.getSourceKey())
                    )
            ));
        }

        String replayKey = request.getSourceKey() + ":replay:" + System.currentTimeMillis();

        boolean firstTime = idempotencyService.markIfFirstTime(replayKey);
        if (!firstTime) {
            return ResponseEntity.ok(new ApiResponseVo<>(
                    "200",
                    "回放请求重复，已忽略",
                    Map.of("idempotencyKey", replayKey, "firstTime", false)
            ));
        }

        messageProducerService.sendMessage(request.getText());
        Integer replayNo = idempotencyService.saveReplayAuditRecord(
                replayKey,
                request.getSourceKey(),
                request.getText(),
                request.getOperator(),
                request.getReason()
        );

        return ResponseEntity.ok(new ApiResponseVo<>(
                "200",
                "回放消息已发送",
                Map.of(
                        "idempotencyKey", replayKey,
                        "sourceKey", request.getSourceKey(),
                        "firstTime", true,
                        "replayNo", replayNo
                )
        ));
    }

    /**
     * 查询回放历史
     */
    @GetMapping("/replay/history")
    public ResponseEntity<ApiResponseVo<Map<String, Object>>> replayHistory(
            @RequestParam("sourceKey") String sourceKey
    ) {
        List<IdempotencyRecord> history = idempotencyService.getReplayHistory(sourceKey);
        return ResponseEntity.ok(new ApiResponseVo<>(
                "200",
                "查询成功",
                Map.of(
                        "sourceKey", sourceKey,
                        "count", history.size(),
                        "items", history
                )
        ));
    }
}
