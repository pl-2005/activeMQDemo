package com.example.pojos.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ApiResponseVo<T> {
    private String code;
    private String message;
    private T data;
}
