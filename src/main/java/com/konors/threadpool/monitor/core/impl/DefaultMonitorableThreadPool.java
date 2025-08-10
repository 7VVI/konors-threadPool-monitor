package com.konors.threadpool.monitor.core.impl;

import com.konors.threadpool.monitor.core.abstraction.MonitorableThreadPool;
import com.konors.threadpool.monitor.core.abstraction.ThreadPoolConfiguration;
import lombok.Builder;
import lombok.Data;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 可监控线程池的默认实现
 * 包装现有的ThreadPoolExecutor，提供监控所需的元数据
 * 
 * @author zhangYh
 * @Date 2025/1/20
 */
@Data
@Builder
public class DefaultMonitorableThreadPool implements MonitorableThreadPool {
    
    private final String name;
    private final ThreadPoolExecutor executor;
    private final ThreadPoolType type;
    private final ThreadPoolConfiguration configuration;
    private final int priority;


    private  Map<String, String> businessTags = new ConcurrentHashMap<>();
    
    @Builder.Default
    private volatile boolean healthy = true;
    
    public DefaultMonitorableThreadPool(String name, ThreadPoolExecutor executor, 
                                       ThreadPoolType type, ThreadPoolConfiguration configuration, 
                                       int priority, Map<String, String> businessTags, boolean healthy) {
        this.name = name;
        this.executor = executor;
        this.type = type != null ? type : ThreadPoolType.CUSTOM;
        this.configuration = configuration;
        this.priority = priority;
        this.healthy = healthy;
        
        if (businessTags != null) {
            this.businessTags.putAll(businessTags);
        }
    }

    @Override
    public String getPoolName() {
        return name;
    }

    @Override
    public ThreadPoolExecutor getExecutor() {
        return executor;
    }

    @Override
    public ThreadPoolType getPoolType() {
        return type;
    }
    
    @Override
    public ThreadPoolConfiguration getConfiguration() {
        return configuration;
    }
    
    @Override
    public boolean isHealthy() {
        if (executor == null) {
            return false;
        }
        
        try {
            // 基本健康检查
            return !executor.isShutdown() && 
                   !executor.isTerminated() && 
                   healthy;
        } catch (Exception e) {
            return false;
        }
    }
    
    @Override
    public Map<String, String> getBusinessTags() {
        return new ConcurrentHashMap<>(businessTags);
    }
    
    @Override
    public int getPriority() {
        return priority;
    }
    
    /**
     * 设置健康状态
     */
    public void setHealthy(boolean healthy) {
        this.healthy = healthy;
    }
    
    /**
     * 添加业务标签
     */
    public void addBusinessTag(String key, String value) {
        if (key != null && value != null) {
            businessTags.put(key, value);
        }
    }
    
    /**
     * 移除业务标签
     */
    public void removeBusinessTag(String key) {
        if (key != null) {
            businessTags.remove(key);
        }
    }
    
    /**
     * 获取业务标签值
     */
    public String getBusinessTag(String key) {
        return businessTags.get(key);
    }
    
    /**
     * 检查是否包含指定标签
     */
    public boolean hasBusinessTag(String key) {
        return businessTags.containsKey(key);
    }
    
    /**
     * 检查是否包含指定标签和值
     */
    public boolean hasBusinessTag(String key, String value) {
        return value != null && value.equals(businessTags.get(key));
    }
    
    /**
     * 获取线程池摘要信息
     */
    public String getSummary() {
        if (executor == null) {
            return String.format("ThreadPool[%s] - INVALID (executor is null)", name);
        }
        
        return String.format(
                "ThreadPool[%s] - Type: %s, Core: %d, Max: %d, Active: %d, Queue: %d, Priority: %d, Healthy: %s",
                name,
                type,
                executor.getCorePoolSize(),
                executor.getMaximumPoolSize(),
                executor.getActiveCount(),
                executor.getQueue().size(),
                priority,
                isHealthy()
        );
    }
    
    /**
     * 创建固定线程池的监控包装
     */
    public static DefaultMonitorableThreadPool wrapFixedThreadPool(String name, 
                                                                   ThreadPoolExecutor executor,
                                                                   int priority) {
        ThreadPoolConfiguration config = ThreadPoolConfiguration.builder()
                .corePoolSize(executor.getCorePoolSize())
                .maximumPoolSize(executor.getMaximumPoolSize())
                .keepAliveTime(executor.getKeepAliveTime(java.util.concurrent.TimeUnit.MILLISECONDS))
                .queueType(executor.getQueue().getClass().getSimpleName())
                .build();
        
        return DefaultMonitorableThreadPool.builder()
                .name(name)
                .executor(executor)
                .type(ThreadPoolType.FIXED)
                .configuration(config)
                .priority(priority)
                .build();
    }
    
    /**
     * 创建缓存线程池的监控包装
     */
    public static DefaultMonitorableThreadPool wrapCachedThreadPool(String name, 
                                                                    ThreadPoolExecutor executor,
                                                                    int priority) {
        ThreadPoolConfiguration config = ThreadPoolConfiguration.builder()
                .corePoolSize(executor.getCorePoolSize())
                .maximumPoolSize(executor.getMaximumPoolSize())
                .keepAliveTime(executor.getKeepAliveTime(java.util.concurrent.TimeUnit.MILLISECONDS))
                .queueType(executor.getQueue().getClass().getSimpleName())
                .build();
        
        return DefaultMonitorableThreadPool.builder()
                .name(name)
                .executor(executor)
                .type(ThreadPoolType.CACHED)
                .configuration(config)
                .priority(priority)
                .build();
    }
    
    /**
     * 创建单线程池的监控包装
     */
    public static DefaultMonitorableThreadPool wrapSingleThreadPool(String name, 
                                                                    ThreadPoolExecutor executor,
                                                                    int priority) {
        ThreadPoolConfiguration config = ThreadPoolConfiguration.builder()
                .corePoolSize(1)
                .maximumPoolSize(1)
                .keepAliveTime(executor.getKeepAliveTime(java.util.concurrent.TimeUnit.MILLISECONDS))
                .queueType(executor.getQueue().getClass().getSimpleName())
                .build();
        
        return DefaultMonitorableThreadPool.builder()
                .name(name)
                .executor(executor)
                .type(ThreadPoolType.SINGLE)
                .configuration(config)
                .priority(priority)
                .build();
    }
    
    /**
     * 创建自定义线程池的监控包装
     */
    public static DefaultMonitorableThreadPool wrapCustomThreadPool(String name, 
                                                                    ThreadPoolExecutor executor,
                                                                    ThreadPoolConfiguration configuration,
                                                                    int priority) {
        return DefaultMonitorableThreadPool.builder()
                .name(name)
                .executor(executor)
                .type(ThreadPoolType.CUSTOM)
                .configuration(configuration)
                .priority(priority)
                .build();
    }
    
    /**
     * 创建带业务标签的监控包装
     */
    public static DefaultMonitorableThreadPool wrapWithTags(String name, 
                                                            ThreadPoolExecutor executor,
                                                            ThreadPoolType type,
                                                            ThreadPoolConfiguration configuration,
                                                            int priority,
                                                            Map<String, String> businessTags) {
        return DefaultMonitorableThreadPool.builder()
                .name(name)
                .executor(executor)
                .type(type)
                .configuration(configuration)
                .priority(priority)
                .businessTags(businessTags != null ? new ConcurrentHashMap<>(businessTags) : new ConcurrentHashMap<>())
                .build();
    }
    
    @Override
    public String toString() {
        return getSummary();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        DefaultMonitorableThreadPool that = (DefaultMonitorableThreadPool) obj;
        return name != null ? name.equals(that.name) : that.name == null;
    }
    
    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }
}