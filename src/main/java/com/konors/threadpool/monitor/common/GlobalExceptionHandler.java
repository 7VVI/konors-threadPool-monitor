package com.konors.threadpool.monitor.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.format.DateTimeParseException;

/**
 * 全局异常处理器
 * @author zhangYh
 * @Date 2025/1/20
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    /**
     * 处理参数校验异常
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Result<Void>> handleValidationException(MethodArgumentNotValidException e) {
        log.error("参数校验异常: {}", e.getMessage());
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .findFirst()
                .orElse("参数校验失败");
        return ResponseEntity.badRequest().body(Result.error(ResultCode.BAD_REQUEST.getCode(), message));
    }
    
    /**
     * 处理参数类型转换异常
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Result<Void>> handleTypeMismatchException(MethodArgumentTypeMismatchException e) {
        log.error("参数类型转换异常: {}", e.getMessage());
        String message = String.format("参数 '%s' 类型错误，期望类型: %s", 
                e.getName(), e.getRequiredType().getSimpleName());
        return ResponseEntity.badRequest().body(Result.error(ResultCode.BAD_REQUEST.getCode(), message));
    }
    
    /**
     * 处理时间解析异常
     */
    @ExceptionHandler(DateTimeParseException.class)
    public ResponseEntity<Result<Void>> handleDateTimeParseException(DateTimeParseException e) {
        log.error("时间解析异常: {}", e.getMessage());
        return ResponseEntity.badRequest().body(Result.error(ResultCode.TIME_RANGE_ERROR.getCode(), 
                "时间格式错误，请使用 yyyy-MM-ddTHH:mm:ss 格式"));
    }
    
    /**
     * 处理业务异常
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Result<Void>> handleBusinessException(BusinessException e) {
        log.error("业务异常: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Result.error(e.getCode(), e.getMessage()));
    }
    
    /**
     * 处理运行时异常
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Result<Void>> handleRuntimeException(RuntimeException e) {
        log.error("运行时异常: ", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Result.error(ResultCode.INTERNAL_SERVER_ERROR.getCode(), "系统内部错误"));
    }
    
    /**
     * 处理其他异常
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result<Void>> handleException(Exception e) {
        log.error("未知异常: ", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Result.error(ResultCode.INTERNAL_SERVER_ERROR.getCode(), "系统异常，请联系管理员"));
    }
}