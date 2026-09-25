package com.atguigo.springaiproject_1.common;

/**
 * 统一响应结构。
 * 所有 HTTP 接口（除 SSE 流式外）都返回这个格式。
 */
public record Result<T>(int code, String message, T data) {

    public static <T> Result<T> success(T data) {
        return new Result<>(200, "success", data);
    }

    public static <T> Result<T> success() {
        return new Result<>(200, "success", null);
    }

    public static <T> Result<T> error(int code, String message) {
        return new Result<>(code, message, null);
    }
}