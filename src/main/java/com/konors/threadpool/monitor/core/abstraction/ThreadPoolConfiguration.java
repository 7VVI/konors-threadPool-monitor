package com.konors.threadpool.monitor.core.abstraction;

import lombok.Builder;
import lombok.Data;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 * 线程池配置信息
 * 封装线程池的所有配置参数，便于监控和管理
 * 
 * @author zhangYh
 * @Date 2025/1/20
 */
@Data
@Builder
public class ThreadPoolConfiguration {
    
    /** 核心线程数 */
    private final int corePoolSize;
    
    /** 最大线程数 */
    private final int maximumPoolSize;
    
    /** 线程空闲存活时间 */
    private final long keepAliveTime;
    
    /** 时间单位 */
    private final TimeUnit timeUnit;
    
    /** 队列类型 */
    private final String queueType;
    
    /** 队列容量 */
    private final int queueCapacity;
    
    /** 拒绝策略类型 */
    private final String rejectedExecutionHandlerType;
    
    /** 线程工厂类型 */
    private final String threadFactoryType;
    
    /** 是否允许核心线程超时 */
    private final boolean allowCoreThreadTimeOut;
    
    /** 是否预启动所有核心线程 */
    private final boolean prestartAllCoreThreads;
    
    /** 扩展属性 */
    private final Map<String, Object> extendedProperties;
    
    /** 创建时间 */
    private final long creationTime;
    
    /** 配置版本 */
    private final String version;
    
    /**
     * 获取线程空闲存活时间（Duration格式）
     */
    public Duration getKeepAliveDuration() {
        return Duration.ofMillis(timeUnit.toMillis(keepAliveTime));
    }
    
    /**
     * 检查配置是否合理
     */
    public boolean isValid() {
        return corePoolSize >= 0 
            && maximumPoolSize > 0 
            && maximumPoolSize >= corePoolSize
            && keepAliveTime >= 0
            && queueCapacity >= 0;
    }
    
    /**
     * 获取配置摘要信息
     */
    public String getConfigSummary() {
        return String.format("core=%d, max=%d, keepAlive=%d%s, queue=%s(%d)",
                corePoolSize, maximumPoolSize, keepAliveTime, 
                timeUnit.toString().toLowerCase(),
                queueType, queueCapacity);
    }
    
    /**
     * 计算理论最大并发数
     */
    public int getMaxConcurrency() {
        return maximumPoolSize + queueCapacity;
    }
    
    /**
     * 获取扩展属性值
     */
    @SuppressWarnings("unchecked")
    public <T> T getExtendedProperty(String key, Class<T> type) {
        Object value = extendedProperties != null ? extendedProperties.get(key) : null;
        if (value != null && type.isInstance(value)) {
            return (T) value;
        }
        return null;
    }
}