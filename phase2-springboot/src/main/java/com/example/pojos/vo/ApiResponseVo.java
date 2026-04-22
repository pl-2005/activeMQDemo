package com.example.pojos.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * API 响应视图对象
 * @param <T>
 */
@Data
@AllArgsConstructor
public class ApiResponseVo<T> {
    private String code;
    private String message;
    private T data;
}
