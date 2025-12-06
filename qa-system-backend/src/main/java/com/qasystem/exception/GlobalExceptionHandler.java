package com.qasystem.exception;

import com.qasystem.common.response.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * 全局异常处理器 - 统一处理系统中的所有异常
 * 
 * 🎯 作用说明：
 * 捕获并处理系统中抛出的各种异常,返回友好的错误信息给前端
 * 就像一个安全网,接住所有的错误,避免系统崩溃
 * 
 * 🔧 工作原理：
 * 1. 当Controller/Service抛出异常时
 * 2. Spring会自动找到对应的@ExceptionHandler方法
 * 3. 执行异常处理逻辑
 * 4. 返回统一格式的错误响应给前端
 * 
 * 💡 处理的异常类型：
 * 1. RuntimeException - 运行时异常(业务异常)
 * 2. MethodArgumentNotValidException - 参数校验失败
 * 3. BindException - 参数绑定异常
 * 4. AccessDeniedException - 权限拒绝
 * 5. Exception - 其他未知异常
 * 
 * 📝 注解说明：
 * @RestControllerAdvice - 全局Controller增强,自动处理所有Controller的异常
 * @ExceptionHandler - 指定处理哪种类型的异常
 * @ResponseStatus - 设置HTTP响应状态码
 * 
 * @author QA System Team
 * @version 1.0
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理运行时异常
     */
    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Object> handleRuntimeException(RuntimeException e) {
        log.error("运行时异常: ", e);
        return Result.error(e.getMessage());
    }

    /**
     * 处理参数校验异常
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Map<String, String>> handleValidationException(MethodArgumentNotValidException e) {
        Map<String, String> errors = new HashMap<>();
        e.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        log.warn("参数校验失败: {}", errors);
        return Result.error(400, "参数校验失败");
    }

    /**
     * 处理绑定异常
     */
    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Object> handleBindException(BindException e) {
        log.warn("参数绑定异常: ", e);
        return Result.error(400, "参数格式错误");
    }

    /**
     * 处理访问拒绝异常
     */
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Result<Object> handleAccessDeniedException(AccessDeniedException e) {
        log.warn("访问被拒绝: ", e);
        return Result.error(403, "没有权限访问该资源");
    }

    /**
     * 处理通用异常
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Object> handleException(Exception e) {
        log.error("系统异常: ", e);
        return Result.error("系统异常，请联系管理员");
    }
}

