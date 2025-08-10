package com.konors.threadpool.monitor.starter;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 线程池监控配置属性
 * 
 * @author zhangYh
 * @Date 2025/1/20
 */
@Data
@ConfigurationProperties(prefix = "konors.threadpool.monitor")
public class ThreadPoolMonitorProperties {

    /**
     * 是否启用监控
     */
    private boolean enabled = true;

    /**
     * 监控间隔时间（毫秒）
     */
    private long monitorInterval = 30000L;

    /**
     * 是否启用异步监控
     */
    private boolean asyncMonitoring = true;

    /**
     * 监控线程池大小
     */
    private int monitorThreadPoolSize = 2;

    /**
     * 是否启用告警
     */
    private boolean alertEnabled = true;

    /**
     * 告警抑制时间（毫秒）
     */
    private long alertSuppressionTime = 300000L;

    /**
     * 是否启用预测性告警
     */
    private boolean predictiveAlertEnabled = false;

    /**
     * 是否启用性能分析
     */
    private boolean performanceAnalysisEnabled = true;

    /**
     * 是否启用健康检查
     */
    private boolean healthCheckEnabled = true;

    /**
     * 默认利用率告警阈值
     */
    private double defaultUtilizationWarningThreshold = 0.8;

    /**
     * 默认利用率严重告警阈值
     */
    private double defaultUtilizationCriticalThreshold = 0.95;

    /**
     * 默认队列告警阈值
     */
    private int defaultQueueWarningThreshold = 100;

    /**
     * 默认队列严重告警阈值
     */
    private int defaultQueueCriticalThreshold = 500;

    /**
     * 是否启用JMX监控
     */
    private boolean jmxEnabled = false;

    /**
     * 是否启用Metrics监控
     */
    private boolean metricsEnabled = false;

    /**
     * 监控数据保留时间（毫秒）
     */
    private long dataRetentionTime = 3600000L; // 1小时
}