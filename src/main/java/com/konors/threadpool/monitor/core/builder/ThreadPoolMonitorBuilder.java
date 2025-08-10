package com.konors.threadpool.monitor.core.builder;

import com.konors.threadpool.monitor.core.abstraction.*;
import com.konors.threadpool.monitor.core.factory.DefaultMonitorStrategyFactory;
import com.konors.threadpool.monitor.core.impl.DefaultAdvancedThreadPoolMonitor;
import com.konors.threadpool.monitor.core.impl.DefaultMonitorableThreadPool;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 线程池监控器构建器
 * 提供流式API来配置和创建线程池监控器
 * 
 * @author zhangYh
 * @Date 2025/1/20
 */
@Slf4j
public class ThreadPoolMonitorBuilder {
    
    private MonitorConfiguration.MonitorConfigurationBuilder configBuilder;
    private MonitorStrategyFactory strategyFactory;
    private final List<MonitorableThreadPool> threadPools = new ArrayList<>();
    private final List<MonitorStrategy> customStrategies = new ArrayList<>();
    private final Map<String, Object> extendedConfig = new HashMap<>();
    
    private ThreadPoolMonitorBuilder() {
        this.configBuilder = MonitorConfiguration.builder();
        this.strategyFactory = new DefaultMonitorStrategyFactory();
    }
    
    /**
     * 创建构建器实例
     */
    public static ThreadPoolMonitorBuilder create() {
        return new ThreadPoolMonitorBuilder();
    }
    
    /**
     * 创建默认配置的构建器
     */
    public static ThreadPoolMonitorBuilder createDefault() {
        return new ThreadPoolMonitorBuilder()
                .withMonitorInterval(Duration.ofSeconds(5))
                .withAlertCheckInterval(Duration.ofSeconds(10))
                .withMetricsCollectionInterval(Duration.ofSeconds(5));
    }
    
    /**
     * 创建高频监控配置的构建器
     */
    public static ThreadPoolMonitorBuilder createHighFrequency() {
        return new ThreadPoolMonitorBuilder()
                .withMonitorInterval(Duration.ofSeconds(1))
                .withAlertCheckInterval(Duration.ofSeconds(2))
                .withMetricsCollectionInterval(Duration.ofSeconds(1))
                .withAdaptiveMonitoring(true);
    }
    
    /**
     * 创建低频监控配置的构建器
     */
    public static ThreadPoolMonitorBuilder createLowFrequency() {
        return new ThreadPoolMonitorBuilder()
                .withMonitorInterval(Duration.ofMinutes(1))
                .withAlertCheckInterval(Duration.ofMinutes(2))
                .withMetricsCollectionInterval(Duration.ofMinutes(1))
                .withMaxHistoryRecords(500);
    }
    
    /**
     * 设置监控间隔
     */
    public ThreadPoolMonitorBuilder withMonitorInterval(Duration interval) {
        configBuilder.monitorInterval(interval);
        return this;
    }
    
    /**
     * 设置告警检查间隔
     */
    public ThreadPoolMonitorBuilder withAlertCheckInterval(Duration interval) {
        configBuilder.alertCheckInterval(interval);
        return this;
    }
    
    /**
     * 设置指标收集间隔
     */
    public ThreadPoolMonitorBuilder withMetricsCollectionInterval(Duration interval) {
        configBuilder.metricsCollectionInterval(interval);
        return this;
    }
    
    /**
     * 设置历史数据保留时间
     */
    public ThreadPoolMonitorBuilder withHistoryRetentionPeriod(Duration period) {
        configBuilder.historyRetentionPeriod(period);
        return this;
    }
    
    /**
     * 设置最大历史记录数
     */
    public ThreadPoolMonitorBuilder withMaxHistoryRecords(int maxRecords) {
        configBuilder.maxHistoryRecords(maxRecords);
        return this;
    }
    
    /**
     * 设置是否启用自适应监控
     */
    public ThreadPoolMonitorBuilder withAdaptiveMonitoring(boolean enabled) {
        configBuilder.adaptiveMonitoringEnabled(enabled);
        return this;
    }
    
    /**
     * 设置是否启用预测性告警
     */
    public ThreadPoolMonitorBuilder withPredictiveAlerting(boolean enabled) {
        configBuilder.predictiveAlertingEnabled(enabled);
        return this;
    }
    
    /**
     * 设置监控线程池大小
     */
    public ThreadPoolMonitorBuilder withMonitorThreadPoolSize(int size) {
        configBuilder.monitorThreadPoolSize(size);
        return this;
    }
    
    /**
     * 设置告警抑制时间
     */
    public ThreadPoolMonitorBuilder withAlertSuppressionPeriod(Duration period) {
        configBuilder.alertSuppressionPeriod(period);
        return this;
    }
    
    /**
     * 设置批量处理大小
     */
    public ThreadPoolMonitorBuilder withBatchSize(int batchSize) {
        configBuilder.batchSize(batchSize);
        return this;
    }
    
    /**
     * 设置异步处理超时时间
     */
    public ThreadPoolMonitorBuilder withAsyncProcessingTimeout(Duration timeout) {
        configBuilder.asyncProcessingTimeout(timeout);
        return this;
    }
    
    /**
     * 设置扩展配置
     */
    public ThreadPoolMonitorBuilder withExtendedConfig(String key, Object value) {
        extendedConfig.put(key, value);
        return this;
    }
    
    /**
     * 设置策略工厂
     */
    public ThreadPoolMonitorBuilder withStrategyFactory(MonitorStrategyFactory factory) {
        this.strategyFactory = factory;
        return this;
    }
    
    /**
     * 添加要监控的线程池
     */
    public ThreadPoolMonitorBuilder addThreadPool(MonitorableThreadPool threadPool) {
        if (threadPool != null) {
            threadPools.add(threadPool);
        }
        return this;
    }
    
    /**
     * 添加要监控的线程池（使用默认包装）
     */
    public ThreadPoolMonitorBuilder addThreadPool(String name, ThreadPoolExecutor executor) {
        return addThreadPool(name, executor, MonitorableThreadPool.ThreadPoolType.CUSTOM, 100);
    }
    
    /**
     * 添加要监控的线程池（指定类型）
     */
    public ThreadPoolMonitorBuilder addThreadPool(String name, ThreadPoolExecutor executor, 
                                                 MonitorableThreadPool.ThreadPoolType type) {
        return addThreadPool(name, executor, type, 100);
    }
    
    /**
     * 添加要监控的线程池（指定类型和优先级）
     */
    public ThreadPoolMonitorBuilder addThreadPool(String name, ThreadPoolExecutor executor, 
                                                 MonitorableThreadPool.ThreadPoolType type, int priority) {
        ThreadPoolConfiguration config = createConfigurationFromExecutor(executor);
        MonitorableThreadPool threadPool = DefaultMonitorableThreadPool.builder()
                .name(name)
                .executor(executor)
                .type(type)
                .configuration(config)
                .priority(priority)
                .build();
        
        return addThreadPool(threadPool);
    }
    
    /**
     * 添加带业务标签的线程池
     */
    public ThreadPoolMonitorBuilder addThreadPoolWithTags(String name, ThreadPoolExecutor executor,
                                                          MonitorableThreadPool.ThreadPoolType type,
                                                          int priority, Map<String, String> businessTags) {
        ThreadPoolConfiguration config = createConfigurationFromExecutor(executor);
        MonitorableThreadPool threadPool = DefaultMonitorableThreadPool.wrapWithTags(
                name, executor, type, config, priority, businessTags);
        
        return addThreadPool(threadPool);
    }
    
    /**
     * 添加自定义监控策略
     */
    public ThreadPoolMonitorBuilder addCustomStrategy(MonitorStrategy strategy) {
        if (strategy != null) {
            customStrategies.add(strategy);
        }
        return this;
    }
    
    /**
     * 添加利用率监控策略
     */
    public ThreadPoolMonitorBuilder addUtilizationStrategy(double warningThreshold, double criticalThreshold) {
        strategyFactory.createStrategy(MonitorStrategyFactory.StrategyType.UTILIZATION_MONITOR,
                DefaultStrategyConfig.createUtilizationConfig(warningThreshold, criticalThreshold))
                .ifPresent(customStrategies::add);
        return this;
    }
    
    /**
     * 添加队列监控策略
     */
    public ThreadPoolMonitorBuilder addQueueStrategy(int warningSize, int criticalSize) {
        strategyFactory.createStrategy(MonitorStrategyFactory.StrategyType.QUEUE_MONITOR,
                DefaultStrategyConfig.createQueueConfig(warningSize, criticalSize))
                .ifPresent(customStrategies::add);
        return this;
    }
    
    /**
     * 添加拒绝任务监控策略
     */
    public ThreadPoolMonitorBuilder addRejectionStrategy(int warningCount, int criticalCount, long timeWindow) {
        strategyFactory.createStrategy(MonitorStrategyFactory.StrategyType.REJECTION_MONITOR,
                DefaultStrategyConfig.createRejectionConfig(warningCount, criticalCount, timeWindow))
                .ifPresent(customStrategies::add);
        return this;
    }
    
    /**
     * 添加健康检查策略
     */
    public ThreadPoolMonitorBuilder addHealthCheckStrategy() {
        strategyFactory.createStrategy(MonitorStrategyFactory.StrategyType.HEALTH_CHECK,
                DefaultStrategyConfig.createHealthCheckConfig())
                .ifPresent(customStrategies::add);
        return this;
    }
    
    /**
     * 添加性能分析策略
     */
    public ThreadPoolMonitorBuilder addPerformanceStrategy(int sampleSize, long analysisInterval) {
        strategyFactory.createStrategy(MonitorStrategyFactory.StrategyType.PERFORMANCE_ANALYSIS,
                DefaultStrategyConfig.createPerformanceConfig(sampleSize, analysisInterval))
                .ifPresent(customStrategies::add);
        return this;
    }
    
    /**
     * 构建监控器
     */
    public AdvancedThreadPoolMonitor build() {
        // 构建配置
        MonitorConfiguration config = configBuilder
                .extendedConfig(new HashMap<>(extendedConfig))
                .build();
        
        // 验证配置
        if (!config.isValid()) {
            throw new IllegalArgumentException("Invalid monitor configuration: " + config.getConfigSummary());
        }
        
        // 创建监控器
        DefaultAdvancedThreadPoolMonitor monitor = new DefaultAdvancedThreadPoolMonitor(config, strategyFactory);
        
        // 注册线程池
        for (MonitorableThreadPool threadPool : threadPools) {
            AdvancedThreadPoolMonitor.RegistrationResult result = monitor.registerThreadPool(threadPool);
            if (!result.isSuccess()) {
                log.warn("Failed to register thread pool {}: {}", threadPool.getPoolName(), result.getMessage());
            }
        }
        
        // 添加自定义策略
        for (MonitorStrategy strategy : customStrategies) {
            monitor.addMonitorStrategy(strategy);
        }
        
        log.info("ThreadPoolMonitor built with {} thread pools and {} custom strategies", 
                threadPools.size(), customStrategies.size());
        
        return monitor;
    }
    
    /**
     * 构建并启动监控器
     */
    public AdvancedThreadPoolMonitor buildAndStart() {
        AdvancedThreadPoolMonitor monitor = build();
        monitor.startMonitoring();
        log.info("ThreadPoolMonitor built and started");
        return monitor;
    }
    
    /**
     * 从执行器创建配置
     */
    private ThreadPoolConfiguration createConfigurationFromExecutor(ThreadPoolExecutor executor) {
        return ThreadPoolConfiguration.builder()
                .corePoolSize(executor.getCorePoolSize())
                .maximumPoolSize(executor.getMaximumPoolSize())
                .keepAliveTime(executor.getKeepAliveTime(java.util.concurrent.TimeUnit.MILLISECONDS))
                .queueType(executor.getQueue().getClass().getSimpleName())
                .queueCapacity(getQueueCapacity(executor))
                .rejectedExecutionHandlerType(executor.getRejectedExecutionHandler().getClass().getSimpleName())
                .threadFactoryType(executor.getThreadFactory().getClass().getSimpleName())
                .build();
    }
    
    /**
     * 获取队列容量
     */
    private int getQueueCapacity(ThreadPoolExecutor executor) {
        try {
            int remaining = executor.getQueue().remainingCapacity();
            int current = executor.getQueue().size();
            return remaining == Integer.MAX_VALUE ? -1 : remaining + current;
        } catch (Exception e) {
            return -1; // 无界队列或获取失败
        }
    }
    
    /**
     * 创建用于Web应用的监控器
     */
    public static ThreadPoolMonitorBuilder forWebApplication() {
        return createDefault()
                .withMonitorInterval(Duration.ofSeconds(3))
                .withAlertCheckInterval(Duration.ofSeconds(5))
                .withAdaptiveMonitoring(true)
                .addUtilizationStrategy(0.8, 0.95)
                .addQueueStrategy(100, 500)
                .addHealthCheckStrategy();
    }
    
    /**
     * 创建用于批处理应用的监控器
     */
    public static ThreadPoolMonitorBuilder forBatchApplication() {
        return createDefault()
                .withMonitorInterval(Duration.ofSeconds(10))
                .withAlertCheckInterval(Duration.ofSeconds(30))
                .withMaxHistoryRecords(5000)
                .addUtilizationStrategy(0.9, 0.98)
                .addQueueStrategy(1000, 5000)
                .addPerformanceStrategy(100, 60000L);
    }
    
    /**
     * 创建用于实时应用的监控器
     */
    public static ThreadPoolMonitorBuilder forRealtimeApplication() {
        return createHighFrequency()
                .withAlertSuppressionPeriod(Duration.ofSeconds(30))
                .withPredictiveAlerting(true)
                .addUtilizationStrategy(0.7, 0.9)
                .addQueueStrategy(50, 200)
                .addHealthCheckStrategy();
    }
}