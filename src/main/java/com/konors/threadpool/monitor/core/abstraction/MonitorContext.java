package com.konors.threadpool.monitor.core.abstraction;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 监控上下文
 * 封装监控过程中的上下文信息，便于策略间共享数据
 * 
 * @author zhangYh
 * @Date 2025/1/20
 */
@Data
@Builder
public class MonitorContext {
    
    /** 监控开始时间 */
    private final LocalDateTime monitorTime;
    
    /** 监控会话ID */
    private final String sessionId;
    
    /** 监控配置 */
    private final MonitorConfiguration configuration;
    
    /** 历史状态数据 */
    private final Map<String, Object> historicalData;
    
    /** 当前监控周期的临时数据 */
    private final Map<String, Object> temporaryData;
    
    /** 全局监控统计 */
    private final GlobalMonitorStats globalStats;
    
    /** 扩展属性 */
    private final Map<String, Object> attributes;
    
    /**
     * 创建默认监控上下文
     */
    public static MonitorContext createDefault() {
        return MonitorContext.builder()
                .monitorTime(LocalDateTime.now())
                .sessionId(java.util.UUID.randomUUID().toString())
                .configuration(MonitorConfiguration.createDefault())
                .historicalData(new ConcurrentHashMap<>())
                .temporaryData(new ConcurrentHashMap<>())
                .globalStats(new GlobalMonitorStats())
                .attributes(new ConcurrentHashMap<>())
                .build();
    }
    
    /**
     * 获取历史数据
     */
    @SuppressWarnings("unchecked")
    public <T> T getHistoricalData(String key, Class<T> type) {
        Object value = historicalData.get(key);
        if (value != null && type.isInstance(value)) {
            return (T) value;
        }
        return null;
    }
    
    /**
     * 设置历史数据
     */
    public void setHistoricalData(String key, Object value) {
        historicalData.put(key, value);
    }
    
    /**
     * 获取临时数据
     */
    @SuppressWarnings("unchecked")
    public <T> T getTemporaryData(String key, Class<T> type) {
        Object value = temporaryData.get(key);
        if (value != null && type.isInstance(value)) {
            return (T) value;
        }
        return null;
    }
    
    /**
     * 设置临时数据
     */
    public void setTemporaryData(String key, Object value) {
        temporaryData.put(key, value);
    }
    
    /**
     * 获取属性值
     */
    @SuppressWarnings("unchecked")
    public <T> T getAttribute(String key, Class<T> type) {
        Object value = attributes.get(key);
        if (value != null && type.isInstance(value)) {
            return (T) value;
        }
        return null;
    }
    
    /**
     * 设置属性值
     */
    public void setAttribute(String key, Object value) {
        attributes.put(key, value);
    }
    
    /**
     * 清理临时数据
     */
    public void clearTemporaryData() {
        temporaryData.clear();
    }
    
    /**
     * 全局监控统计
     */
    @Data
    public static class GlobalMonitorStats {
        /** 总监控线程池数量 */
        private volatile int totalMonitoredPools = 0;
        /** 健康线程池数量 */
        private volatile int healthyPools = 0;
        /** 告警线程池数量 */
        private volatile int alertingPools = 0;
        /** 监控周期计数 */
        private volatile long monitorCycles = 0;
        /** 最后更新时间 */
        private volatile LocalDateTime lastUpdateTime = LocalDateTime.now();
        
        public void incrementMonitorCycles() {
            monitorCycles++;
            lastUpdateTime = LocalDateTime.now();
        }
        
        public void updatePoolCounts(int total, int healthy, int alerting) {
            this.totalMonitoredPools = total;
            this.healthyPools = healthy;
            this.alertingPools = alerting;
            this.lastUpdateTime = LocalDateTime.now();
        }
        
        public double getHealthyRate() {
            return totalMonitoredPools > 0 ? (double) healthyPools / totalMonitoredPools : 1.0;
        }
    }
}