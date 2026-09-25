package com.atguigo.springaiproject_1.common;

/**
 * 业务异常。
 * 抛出它会被 GlobalExceptionHandler 捕获，返回给前端一个明确的错误信息。
 */
public class BusinessException extends RuntimeException {

    private final int code;

    public BusinessException(String message) {
        this(400, message);
    }

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}