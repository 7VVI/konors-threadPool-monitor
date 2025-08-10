package com.konors.threadpool.monitor.core.abstraction;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * 监控策略工厂接口
 * 负责创建和管理各种监控策略
 * 
 * @author zhangYh
 * @Date 2025/1/20
 */
public interface MonitorStrategyFactory {
    
    /**
     * 创建监控策略
     * @param strategyType 策略类型
     * @param config 策略配置
     * @return 监控策略实例
     */
    Optional<MonitorStrategy> createStrategy(StrategyType strategyType, StrategyConfig config);
    
    /**
     * 根据名称创建策略
     * @param strategyName 策略名称
     * @param config 策略配置
     * @return 监控策略实例
     */
    Optional<MonitorStrategy> createStrategy(String strategyName, StrategyConfig config);
    
    /**
     * 创建默认策略集合
     * @return 默认策略列表
     */
    List<MonitorStrategy> createDefaultStrategies();
    
    /**
     * 创建针对特定线程池类型的策略
     * @param threadPoolType 线程池类型
     * @return 适用的策略列表
     */
    List<MonitorStrategy> createStrategiesForThreadPoolType(MonitorableThreadPool.ThreadPoolType threadPoolType);
    
    /**
     * 注册自定义策略创建器
     * @param strategyType 策略类型
     * @param creator 策略创建器
     */
    void registerStrategyCreator(StrategyType strategyType, StrategyCreator creator);
    
    /**
     * 注册自定义策略创建器
     * @param strategyName 策略名称
     * @param creator 策略创建器
     */
    void registerStrategyCreator(String strategyName, StrategyCreator creator);
    
    /**
     * 获取所有支持的策略类型
     * @return 策略类型集合
     */
    Set<StrategyType> getSupportedStrategyTypes();
    
    /**
     * 获取所有注册的策略名称
     * @return 策略名称集合
     */
    Set<String> getRegisteredStrategyNames();
    
    /**
     * 检查是否支持指定策略类型
     * @param strategyType 策略类型
     * @return 是否支持
     */
    boolean supportsStrategyType(StrategyType strategyType);
    
    /**
     * 检查是否支持指定策略名称
     * @param strategyName 策略名称
     * @return 是否支持
     */
    boolean supportsStrategyName(String strategyName);
    
    /**
     * 策略类型枚举
     */
    enum StrategyType {
        /** 利用率监控 */
        UTILIZATION_MONITOR,
        /** 队列监控 */
        QUEUE_MONITOR,
        /** 拒绝任务监控 */
        REJECTION_MONITOR,
        /** 响应时间监控 */
        RESPONSE_TIME_MONITOR,
        /** 健康检查 */
        HEALTH_CHECK,
        /** 性能分析 */
        PERFORMANCE_ANALYSIS,
        /** 趋势预测 */
        TREND_PREDICTION,
        /** 异常检测 */
        ANOMALY_DETECTION,
        /** 自定义策略 */
        CUSTOM
    }
    
    /**
     * 策略配置接口
     */
    interface StrategyConfig {
        /**
         * 获取配置参数
         * @param key 参数键
         * @return 参数值
         */
        Object getParameter(String key);
        
        /**
         * 获取配置参数，带默认值
         * @param key 参数键
         * @param defaultValue 默认值
         * @param <T> 参数类型
         * @return 参数值
         */
        <T> T getParameter(String key, T defaultValue);
        
        /**
         * 设置配置参数
         * @param key 参数键
         * @param value 参数值
         */
        void setParameter(String key, Object value);
        
        /**
         * 获取所有参数键
         * @return 参数键集合
         */
        Set<String> getParameterKeys();
        
        /**
         * 验证配置有效性
         * @return 是否有效
         */
        boolean isValid();
    }
    
    /**
     * 策略创建器接口
     */
    @FunctionalInterface
    interface StrategyCreator {
        /**
         * 创建策略实例
         * @param config 策略配置
         * @return 策略实例
         */
        MonitorStrategy create(StrategyConfig config);
    }
}