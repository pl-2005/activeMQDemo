package com.example.pojos.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 重发请求
 */
@Data
public class ReplayRequestDto {

    @NotBlank(message = "sourceKey不能为空")
    private String sourceKey;

    @NotBlank(message = "text不能为空")
    private String text;

    @NotBlank(message = "operator不能为空")
    private String operator;

    @NotBlank(message = "reason不能为空")
    private String reason;
}
