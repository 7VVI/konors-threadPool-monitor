package com.konors.threadpool.monitor.core.abstraction;

import lombok.Builder;
import lombok.Data;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 监控配置
 * 定义监控行为的各种配置参数
 * 
 * @author zhangYh
 * @Date 2025/1/20
 */
@Data
@Builder
public class MonitorConfiguration {
    
    /** 监控间隔 */
    @Builder.Default
    private Duration monitorInterval = Duration.ofSeconds(5);
    
    /** 告警检查间隔 */
    @Builder.Default
    private Duration alertCheckInterval = Duration.ofSeconds(10);
    
    /** 指标收集间隔 */
    @Builder.Default
    private Duration metricsCollectionInterval = Duration.ofSeconds(5);
    
    /** 历史数据保留时间 */
    @Builder.Default
    private Duration historyRetentionPeriod = Duration.ofHours(24);
    
    /** 最大历史记录数 */
    @Builder.Default
    private int maxHistoryRecords = 2000;
    
    /** 是否启用自适应监控 */
    @Builder.Default
    private boolean adaptiveMonitoringEnabled = true;
    
    /** 是否启用预测性告警 */
    @Builder.Default
    private boolean predictiveAlertingEnabled = false;
    
    /** 监控线程池大小 */
    @Builder.Default
    private int monitorThreadPoolSize = 2;
    
    /** 告警抑制时间 */
    @Builder.Default
    private Duration alertSuppressionPeriod = Duration.ofMinutes(5);
    
    /** 批量处理大小 */
    @Builder.Default
    private int batchSize = 100;
    
    /** 异步处理超时时间 */
    @Builder.Default
    private Duration asyncProcessingTimeout = Duration.ofSeconds(30);
    
    /** 扩展配置 */
    @Builder.Default
    private Map<String, Object> extendedConfig = new ConcurrentHashMap<>();
    
    /**
     * 创建默认配置
     */
    public static MonitorConfiguration createDefault() {
        return MonitorConfiguration.builder().build();
    }
    
    /**
     * 创建高频监控配置
     */
    public static MonitorConfiguration createHighFrequency() {
        return MonitorConfiguration.builder()
                .monitorInterval(Duration.ofSeconds(1))
                .alertCheckInterval(Duration.ofSeconds(2))
                .metricsCollectionInterval(Duration.ofSeconds(1))
                .build();
    }
    
    /**
     * 创建低频监控配置
     */
    public static MonitorConfiguration createLowFrequency() {
        return MonitorConfiguration.builder()
                .monitorInterval(Duration.ofMinutes(1))
                .alertCheckInterval(Duration.ofMinutes(2))
                .metricsCollectionInterval(Duration.ofMinutes(1))
                .build();
    }
    
    /**
     * 获取扩展配置值
     */
    @SuppressWarnings("unchecked")
    public <T> T getExtendedConfig(String key, Class<T> type, T defaultValue) {
        Object value = extendedConfig.get(key);
        if (value != null && type.isInstance(value)) {
            return (T) value;
        }
        return defaultValue;
    }
    
    /**
     * 设置扩展配置
     */
    public void setExtendedConfig(String key, Object value) {
        extendedConfig.put(key, value);
    }
    
    /**
     * 验证配置有效性
     */
    public boolean isValid() {
        return monitorInterval != null && !monitorInterval.isNegative()
            && alertCheckInterval != null && !alertCheckInterval.isNegative()
            && metricsCollectionInterval != null && !metricsCollectionInterval.isNegative()
            && historyRetentionPeriod != null && !historyRetentionPeriod.isNegative()
            && maxHistoryRecords > 0
            && monitorThreadPoolSize > 0
            && batchSize > 0;
    }
    
    /**
     * 获取配置摘要
     */
    public String getConfigSummary() {
        return String.format("monitor=%dms, alert=%dms, metrics=%dms, history=%d",
                monitorInterval.toMillis(),
                alertCheckInterval.toMillis(),
                metricsCollectionInterval.toMillis(),
                maxHistoryRecords);
    }
}