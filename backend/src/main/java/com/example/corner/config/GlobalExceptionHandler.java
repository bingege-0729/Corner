package com.example.corner.config;

import com.example.corner.common.Result;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理业务异常（如地点不存在、网络搜索结果等）
     */
    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.OK) // 返回 HTTP 200，通过 code 区分
    public Result<?> handleRuntimeException(RuntimeException e) {
        String message = e.getMessage();

        // 针对网络搜索地点的友好提示
        if (message != null
                && (message.contains("网络搜索结果") || message.contains("暂无详细信息") || message.contains("无法生成出行提示"))) {
            return Result.error(400, message);
        }

        // 其他业务异常
        return Result.error(400, message != null ? message : "请求失败");
    }

    /**
     * 处理所有其他异常
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<?> handleException(Exception e) {
        // 记录日志（生产环境应该使用 logger）
        System.err.println("系统异常: " + e.getMessage());
        e.printStackTrace();

        return Result.error(500, "服务器内部错误，请稍后重试");
    }
}
