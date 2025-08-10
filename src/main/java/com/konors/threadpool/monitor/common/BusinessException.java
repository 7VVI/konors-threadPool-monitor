package com.konors.threadpool.monitor.common;

import lombok.Getter;

/**
 * 业务异常类
 * @author zhangYh
 * @Date 2025/1/20
 */
@Getter
public class BusinessException extends RuntimeException {
    
    /**
     * 错误码
     */
    private final Integer code;
    
    /**
     * 错误消息
     */
    private final String message;
    
    public BusinessException(String message) {
        super(message);
        this.code = ResultCode.INTERNAL_SERVER_ERROR.getCode();
        this.message = message;
    }
    
    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }
    
    public BusinessException(ResultCode resultCode) {
        super(resultCode.getMessage());
        this.code = resultCode.getCode();
        this.message = resultCode.getMessage();
    }
    
    public BusinessException(ResultCode resultCode, String customMessage) {
        super(customMessage);
        this.code = resultCode.getCode();
        this.message = customMessage;
    }
    
    /**
     * 创建线程池未找到异常
     */
    public static BusinessException threadPoolNotFound(String poolName) {
        return new BusinessException(ResultCode.THREAD_POOL_NOT_FOUND.getCode(), 
                String.format("线程池 '%s' 未找到", poolName));
    }
    
    /**
     * 创建配置错误异常
     */
    public static BusinessException configError(String message) {
        return new BusinessException(ResultCode.THREAD_POOL_CONFIG_ERROR.getCode(), message);
    }
    
    /**
     * 创建告警配置错误异常
     */
    public static BusinessException alertConfigError(String message) {
        return new BusinessException(ResultCode.ALERT_CONFIG_ERROR.getCode(), message);
    }
    
    /**
     * 创建指标收集错误异常
     */
    public static BusinessException metricsError(String message) {
        return new BusinessException(ResultCode.METRICS_COLLECTION_ERROR.getCode(), message);
    }
}