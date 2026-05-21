package com.example.corner.common;

import lombok.Getter;

/**
 * 响应状态码枚举
 */
@Getter
public enum ResultCode {

    // 成功
    SUCCESS(200, "操作成功"),

    // 客户端错误 4xx
    BAD_REQUEST(400, "请求参数错误"), UNAUTHORIZED(401, "未授权，请先登录"), FORBIDDEN(403, "禁止访问"), NOT_FOUND(404,
            "资源不存在"), METHOD_NOT_ALLOWED(405, "请求方法不允许"),

    // 服务端错误 5xx
    INTERNAL_SERVER_ERROR(500, "服务器内部错误"), SERVICE_UNAVAILABLE(503, "服务不可用"),

    // 业务错误 1xxx
    BUSINESS_ERROR(1000, "业务处理失败"), DATA_NOT_FOUND(1001, "数据不存在"), DATA_ALREADY_EXISTS(1002,
            "数据已存在"), VALIDATION_ERROR(1003, "数据验证失败"),

    // 用户相关 2xxx
    USER_NOT_FOUND(2001, "用户不存在"), USER_ALREADY_EXISTS(2002, "用户已存在"), PASSWORD_ERROR(2003, "密码错误"), TOKEN_INVALID(2004,
            "Token无效或已过期"), TOKEN_EXPIRED(2005, "Token已过期");

    private final Integer code;
    private final String message;

    ResultCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}
