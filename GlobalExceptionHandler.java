package com.atguigo.springaiproject_1.common;

import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    /** 业务异常：自己 throw 的 */
    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusiness(BusinessException e) {
        System.err.println(">>> [EX] 业务异常: " + e.getMessage());
        return Result.error(e.getCode(), e.getMessage());
    }

    /** 参数校验失败：@NotBlank、@NotNull 等触发 */
    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    public Result<Void> handleValidation(BindException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(FieldError::getDefaultMessage)
                .orElse("参数校验失败");
        System.err.println(">>> [EX] 参数校验失败: " + msg);
        return Result.error(400, msg);
    }

    /** 缺少请求参数：@RequestParam 必填但没传 */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public Result<Void> handleMissingParam(MissingServletRequestParameterException e) {
        String msg = "缺少参数: " + e.getParameterName();
        System.err.println(">>> [EX] " + msg);
        return Result.error(400, msg);
    }

    /** 兜底：所有未捕获的异常 */
    @ExceptionHandler(Exception.class)
    public Result<Void> handleAll(Exception e) {
        System.err.println(">>> [EX] 系统异常: " + e.getMessage());
        e.printStackTrace();
        return Result.error(500, "系统繁忙，请稍后重试");
    }
}