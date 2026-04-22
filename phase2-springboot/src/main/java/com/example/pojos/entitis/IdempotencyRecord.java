package com.example.pojos.entitis;

import com.mybatisflex.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 幂等记录实体
 */
@Data
@Table("idempotency_record")
public class IdempotencyRecord {

    @Id(keyType = KeyType.Auto)
    private Long id;

    private String idempotencyKey;

    private String payload;

    private String status;

    private String sourceKey;

    private Integer replayNo;

    private String replayOperator;

    private String replayReason;

    @Column(onInsertValue = "now()")
    private LocalDateTime createdAt;

    @Column(onInsertValue = "now()", onUpdateValue = "now()")
    private LocalDateTime updatedAt;
}
