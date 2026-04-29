package com.example.controllers;

import com.example.constants.ApiCode;
import com.example.constants.ApiMessage;
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
            return ResponseEntity.ok(ApiResponseVo.fail(ApiCode.IDEMPOTENT_DUPLICATE, ApiMessage.SEND_DUPLICATE, null));
        }

        messageProducerService.sendMessage(request.getText());
        idempotencyService.saveAuditRecord(idempotencyKey, request.getText());

        return ResponseEntity.ok(ApiResponseVo.success(ApiMessage.SEND_SUCCESS, Map.of(
                "queue", "demo.queue.boot",
                "text", request.getText(),
                "idempotencyKey", idempotencyKey,
                "firstTime", true
        )));
    }

    /**
     * 回放消息
     * @param request 回放请求参数
     * @return 回放结果参数
     */
    @PostMapping("/replay")
    public ResponseEntity<ApiResponseVo<Map<String, Object>>> replay(@Valid @RequestBody ReplayRequestDto request) {
        if (!idempotencyService.canReplay(request.getSourceKey())) {
            return ResponseEntity.ok(ApiResponseVo.fail(ApiCode.REPLAY_LIMIT_EXCEEDED, ApiMessage.REPLAY_LIMIT_EXCEEDED, null));
        }

        String replayKey = request.getSourceKey() + ":replay:" + System.currentTimeMillis();

        boolean firstTime = idempotencyService.markIfFirstTime(replayKey);
        if (!firstTime) {
            return ResponseEntity.ok(ApiResponseVo.fail(ApiCode.IDEMPOTENT_DUPLICATE, ApiMessage.REPLAY_DUPLICATE, null));
        }

        messageProducerService.sendMessage(request.getText());
        Integer replayNo = idempotencyService.saveReplayAuditRecord(
                replayKey,
                request.getSourceKey(),
                request.getText(),
                request.getOperator(),
                request.getReason()
        );

        return ResponseEntity.ok(ApiResponseVo.success(ApiMessage.REPLAY_SUCCESS, Map.of(
                "idempotencyKey", replayKey,
                "sourceKey", request.getSourceKey(),
                "firstTime", true,
                "replayNo", replayNo
        )));
    }

    /**
     * 查询回放历史
     */
    @GetMapping("/replay/history")
    public ResponseEntity<ApiResponseVo<Map<String, Object>>> replayHistory(
            @RequestParam("sourceKey") String sourceKey
    ) {
        List<IdempotencyRecord> history = idempotencyService.getReplayHistory(sourceKey);
        return ResponseEntity.ok(ApiResponseVo.success(ApiMessage.QUERY_SUCCESS, Map.of(
                "sourceKey", sourceKey,
                "count", history.size(),
                "items", history
        )));
    }
}
