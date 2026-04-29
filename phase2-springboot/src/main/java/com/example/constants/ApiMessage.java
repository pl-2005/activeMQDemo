package com.example.constants;

public final class ApiMessage {

    private ApiMessage() {}

    public static final String SEND_SUCCESS = "消息已发送";
    public static final String SEND_DUPLICATE = "重复请求，已忽略";

    public static final String REPLAY_SUCCESS = "回放消息已发送";
    public static final String REPLAY_DUPLICATE = "回放请求重复，已忽略";
    public static final String REPLAY_LIMIT_EXCEEDED = "超过最大回放次数，已拒绝";

    public static final String QUERY_SUCCESS = "查询成功";
}