package com.example.pojos.entitis;

import com.mybatisflex.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Table("idempotency_record")
public class IdempotencyRecord {

    @Id(keyType = KeyType.Auto)
    private Long id;

    private String idempotencyKey;

    private String payload;

    @Column(onInsertValue = "now()")
    private LocalDateTime createdAt;

}
