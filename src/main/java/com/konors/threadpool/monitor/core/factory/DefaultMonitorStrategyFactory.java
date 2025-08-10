package com.konors.threadpool.monitor.core.factory;

import com.konors.threadpool.monitor.core.ThreadPoolStatus;
import com.konors.threadpool.monitor.core.abstraction.*;
import com.konors.threadpool.monitor.core.strategy.*;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 默认监控策略工厂实现
 * 提供内置策略的创建和自定义策略的注册功能
 * 
 * @author zhangYh
 * @Date 2025/1/20
 */
@Slf4j
public class DefaultMonitorStrategyFactory implements MonitorStrategyFactory {
    
    private final Map<StrategyType, StrategyCreator> typeCreators = new ConcurrentHashMap<>();
    private final Map<String, StrategyCreator> nameCreators = new ConcurrentHashMap<>();
    
    public DefaultMonitorStrategyFactory() {
        initializeBuiltinStrategies();
    }
    
    /**
     * 初始化内置策略
     */
    private void initializeBuiltinStrategies() {
        // 注册内置策略创建器
        registerStrategyCreator(StrategyType.UTILIZATION_MONITOR, UtilizationMonitorStrategy::new);
        registerStrategyCreator(StrategyType.QUEUE_MONITOR, QueueMonitorStrategy::new);
        registerStrategyCreator(StrategyType.REJECTION_MONITOR, this::createRejectionMonitorStrategy);
        registerStrategyCreator(StrategyType.HEALTH_CHECK, this::createHealthCheckStrategy);
        registerStrategyCreator(StrategyType.PERFORMANCE_ANALYSIS, this::createPerformanceAnalysisStrategy);
        
        // 注册名称映射
        registerStrategyCreator("utilization", UtilizationMonitorStrategy::new);
        registerStrategyCreator("queue", QueueMonitorStrategy::new);
        registerStrategyCreator("rejection", this::createRejectionMonitorStrategy);
        registerStrategyCreator("health", this::createHealthCheckStrategy);
        registerStrategyCreator("performance", this::createPerformanceAnalysisStrategy);
        
        log.info("DefaultMonitorStrategyFactory initialized with {} built-in strategies", typeCreators.size());
    }
    
    @Override
    public Optional<MonitorStrategy> createStrategy(StrategyType strategyType, StrategyConfig config) {
        StrategyCreator creator = typeCreators.get(strategyType);
        if (creator == null) {
            log.warn("No creator found for strategy type: {}", strategyType);
            return Optional.empty();
        }
        
        try {
            MonitorStrategy strategy = creator.create(config);
            log.debug("Created strategy: {} with type: {}", strategy.getStrategyName(), strategyType);
            return Optional.of(strategy);
        } catch (Exception e) {
            log.error("Failed to create strategy for type: {}", strategyType, e);
            return Optional.empty();
        }
    }
    
    @Override
    public Optional<MonitorStrategy> createStrategy(String strategyName, StrategyConfig config) {
        StrategyCreator creator = nameCreators.get(strategyName.toLowerCase());
        if (creator == null) {
            log.warn("No creator found for strategy name: {}", strategyName);
            return Optional.empty();
        }
        
        try {
            MonitorStrategy strategy = creator.create(config);
            log.debug("Created strategy: {} with name: {}", strategy.getStrategyName(), strategyName);
            return Optional.of(strategy);
        } catch (Exception e) {
            log.error("Failed to create strategy for name: {}", strategyName, e);
            return Optional.empty();
        }
    }
    
    @Override
    public List<MonitorStrategy> createDefaultStrategies() {
        List<MonitorStrategy> strategies = new ArrayList<>();
        
        // 创建默认的利用率监控策略
        createStrategy(StrategyType.UTILIZATION_MONITOR, 
                DefaultStrategyConfig.createUtilizationConfig(0.8, 0.95))
                .ifPresent(strategies::add);
        
        // 创建默认的队列监控策略
        createStrategy(StrategyType.QUEUE_MONITOR, 
                DefaultStrategyConfig.createQueueConfig(100, 500))
                .ifPresent(strategies::add);
        
        // 创建默认的拒绝任务监控策略
        createStrategy(StrategyType.REJECTION_MONITOR, 
                DefaultStrategyConfig.createRejectionConfig(10, 50, 60000L))
                .ifPresent(strategies::add);
        
        // 创建默认的健康检查策略
        createStrategy(StrategyType.HEALTH_CHECK, 
                DefaultStrategyConfig.createHealthCheckConfig())
                .ifPresent(strategies::add);
        
        log.info("Created {} default strategies", strategies.size());
        return strategies;
    }
    
    @Override
    public List<MonitorStrategy> createStrategiesForThreadPoolType(MonitorableThreadPool.ThreadPoolType threadPoolType) {
        List<MonitorStrategy> strategies = new ArrayList<>();
        
        switch (threadPoolType) {
            case FIXED:
                // 固定线程池重点关注队列和利用率
                createStrategy(StrategyType.UTILIZATION_MONITOR, 
                        DefaultStrategyConfig.createUtilizationConfig(0.9, 0.98))
                        .ifPresent(strategies::add);
                createStrategy(StrategyType.QUEUE_MONITOR, 
                        DefaultStrategyConfig.createQueueConfig(200, 1000))
                        .ifPresent(strategies::add);
                break;
                
            case CACHED:
                // 缓存线程池重点关注线程创建和回收
                createStrategy(StrategyType.PERFORMANCE_ANALYSIS, 
                        DefaultStrategyConfig.createPerformanceConfig(50, 30000L))
                        .ifPresent(strategies::add);
                createStrategy(StrategyType.HEALTH_CHECK, 
                        DefaultStrategyConfig.createHealthCheckConfig())
                        .ifPresent(strategies::add);
                break;
                
            case SCHEDULED:
                // 调度线程池重点关注任务执行时间
                createStrategy(StrategyType.UTILIZATION_MONITOR, 
                        DefaultStrategyConfig.createUtilizationConfig(0.7, 0.9))
                        .ifPresent(strategies::add);
                createStrategy(StrategyType.PERFORMANCE_ANALYSIS, 
                        DefaultStrategyConfig.createPerformanceConfig(100, 60000L))
                        .ifPresent(strategies::add);
                break;
                
            case SINGLE:
                // 单线程池重点关注任务积压
                createStrategy(StrategyType.QUEUE_MONITOR, 
                        DefaultStrategyConfig.createQueueConfig(50, 200))
                        .ifPresent(strategies::add);
                createStrategy(StrategyType.HEALTH_CHECK, 
                        DefaultStrategyConfig.createHealthCheckConfig())
                        .ifPresent(strategies::add);
                break;
                
            case CUSTOM:
            default:
                // 自定义线程池使用默认策略
                strategies.addAll(createDefaultStrategies());
                break;
        }
        
        log.debug("Created {} strategies for thread pool type: {}", strategies.size(), threadPoolType);
        return strategies;
    }
    
    @Override
    public void registerStrategyCreator(StrategyType strategyType, StrategyCreator creator) {
        if (strategyType != null && creator != null) {
            typeCreators.put(strategyType, creator);
            log.debug("Registered strategy creator for type: {}", strategyType);
        }
    }
    
    @Override
    public void registerStrategyCreator(String strategyName, StrategyCreator creator) {
        if (strategyName != null && !strategyName.trim().isEmpty() && creator != null) {
            nameCreators.put(strategyName.toLowerCase().trim(), creator);
            log.debug("Registered strategy creator for name: {}", strategyName);
        }
    }
    
    @Override
    public Set<StrategyType> getSupportedStrategyTypes() {
        return new HashSet<>(typeCreators.keySet());
    }
    
    @Override
    public Set<String> getRegisteredStrategyNames() {
        return new HashSet<>(nameCreators.keySet());
    }
    
    @Override
    public boolean supportsStrategyType(StrategyType strategyType) {
        return typeCreators.containsKey(strategyType);
    }
    
    @Override
    public boolean supportsStrategyName(String strategyName) {
        return strategyName != null && nameCreators.containsKey(strategyName.toLowerCase());
    }
    
    /**
     * 创建拒绝任务监控策略（占位实现）
     */
    private MonitorStrategy createRejectionMonitorStrategy(StrategyConfig config) {
        // 这里应该实现具体的拒绝任务监控策略
        // 为了演示，返回一个简单的实现
        return new SimpleMonitorStrategy("RejectionMonitor", 80);
    }
    
    /**
     * 创建健康检查策略（占位实现）
     */
    private MonitorStrategy createHealthCheckStrategy(StrategyConfig config) {
        // 这里应该实现具体的健康检查策略
        return new SimpleMonitorStrategy("HealthCheck", 70);
    }
    
    /**
     * 创建性能分析策略（占位实现）
     */
    private MonitorStrategy createPerformanceAnalysisStrategy(StrategyConfig config) {
        // 这里应该实现具体的性能分析策略
        return new SimpleMonitorStrategy("PerformanceAnalysis", 60);
    }
    
    /**
     * 简单监控策略实现（用于占位）
     */
    private static class SimpleMonitorStrategy implements MonitorStrategy {
        private final String name;
        private final int priority;
        
        public SimpleMonitorStrategy(String name, int priority) {
            this.name = name;
            this.priority = priority;
        }
        
        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getStrategyName() {
            return null;
        }

        @Override
        public int getPriority() {
            return priority;
        }
        
        @Override
        public boolean supports(MonitorableThreadPool threadPool) {
            return true;
        }

        @Override
        public MonitorResult monitor(MonitorableThreadPool threadPool, ThreadPoolStatus status, MonitorContext context) {
            return null;
        }

        @Override
        public MonitorResult monitor(MonitorableThreadPool threadPool, MonitorContext context) {
            // 简单的占位实现
            return new MonitorResult() {

                @Override
                public boolean shouldAlert() {
                    return true;
                }

                @Override
                public AlertLevel getAlertLevel() {
                    return AlertLevel.INFO;
                }
                
                @Override
                public String getMessage() {
                    return name + " monitoring completed for " + threadPool.getPoolName();
                }
                
                @Override
                public String getSuggestedAction() {
                    return "No action required";
                }
                
                @Override
                public Map<String, Object> getExtendedData() {
                    return new HashMap<>();
                }
            };
        }
    }
}