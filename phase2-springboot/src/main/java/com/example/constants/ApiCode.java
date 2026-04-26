package com.example.constants;

/**
 * 业务响应码（与 HTTP 状态码解耦）
 */
public final class ApiCode {
    private ApiCode() {}

    /** 成功 */
    public static final String SUCCESS = "200";

    /** 幂等重复请求（发送/回放重复） */
    public static final String IDEMPOTENT_DUPLICATE = "40901";

    /** 回放次数超限 */
    public static final String REPLAY_LIMIT_EXCEEDED = "40902";

    /** 参数校验失败（预留） */
    public static final String VALIDATION_ERROR = "40001";

    /** 系统异常（预留） */
    public static final String INTERNAL_ERROR = "50000";
}

