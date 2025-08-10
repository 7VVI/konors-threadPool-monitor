package com.konors.threadpool.monitor.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 响应状态码枚举
 * @author zhangYh
 * @Date 2025/1/20
 */
@Getter
@AllArgsConstructor
public enum ResultCode {
    
    /**
     * 成功
     */
    SUCCESS(200, "操作成功"),
    
    /**
     * 客户端错误
     */
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未授权"),
    FORBIDDEN(403, "禁止访问"),
    NOT_FOUND(404, "资源未找到"),
    METHOD_NOT_ALLOWED(405, "请求方法不允许"),
    
    /**
     * 服务端错误
     */
    INTERNAL_SERVER_ERROR(500, "服务器内部错误"),
    SERVICE_UNAVAILABLE(503, "服务不可用"),
    
    /**
     * 业务错误
     */
    THREAD_POOL_NOT_FOUND(1001, "线程池未找到"),
    THREAD_POOL_CONFIG_ERROR(1002, "线程池配置错误"),
    ALERT_CONFIG_ERROR(1003, "告警配置错误"),
    METRICS_COLLECTION_ERROR(1004, "指标收集错误"),
    TIME_RANGE_ERROR(1005, "时间范围参数错误");
    
    /**
     * 状态码
     */
    private final Integer code;
    
    /**
     * 消息
     */
    private final String message;
}