package com.example.pojos.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class MessageSendDto {

    @NotBlank(message = "text 不能为空")
    @Size(max = 200, message = "text 最大长度不能超过 200")
    private String text;
}
