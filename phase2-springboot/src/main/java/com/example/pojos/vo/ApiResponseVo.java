package com.example.pojos.vo;

import com.example.constants.ApiCode;
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

    public static <T> ApiResponseVo<T> success(String message, T data) {
        return new ApiResponseVo<>(ApiCode.SUCCESS, message, data);
    }

    public static <T> ApiResponseVo<T> fail(String code, String message, T data) {
        return new ApiResponseVo<>(code, message, data);
    }
}
