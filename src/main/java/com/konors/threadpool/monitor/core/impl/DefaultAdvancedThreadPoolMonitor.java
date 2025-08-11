package com.konors.threadpool.monitor.core.impl;

import com.konors.threadpool.monitor.core.ThreadPoolStatus;
import com.konors.threadpool.monitor.core.abstraction.*;
import com.konors.threadpool.monitor.core.factory.DefaultMonitorStrategyFactory;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * 高级线程池监控器的默认实现
 * 提供完整的线程池监控功能，支持策略化监控和异步处理
 * 
 * @author zhangYh
 * @Date 2025/1/20
 */
@Slf4j
public class DefaultAdvancedThreadPoolMonitor implements AdvancedThreadPoolMonitor {
    
    private final Map<String, MonitorableThreadPool> registeredPools = new ConcurrentHashMap<>();
    private final Map<String, MonitorStrategy> strategies = new ConcurrentHashMap<>();
    private final MonitorStrategyFactory strategyFactory;
    private final MonitorConfiguration configuration;
    private final ScheduledExecutorService monitorExecutor;
    private final ExecutorService asyncExecutor;
    
    private volatile MonitoringState currentState = MonitoringState.NOT_STARTED;
    private volatile ScheduledFuture<?> monitoringTask;
    private final Object stateLock = new Object();
    
    // 统计信息
    private final MonitorStatisticsImpl statistics = new MonitorStatisticsImpl();
    
    public DefaultAdvancedThreadPoolMonitor() {
        this(MonitorConfiguration.createDefault(), new DefaultMonitorStrategyFactory());
    }
    
    public DefaultAdvancedThreadPoolMonitor(MonitorConfiguration configuration) {
        this(configuration, new DefaultMonitorStrategyFactory());
    }
    
    public DefaultAdvancedThreadPoolMonitor(MonitorConfiguration configuration, 
                                           MonitorStrategyFactory strategyFactory) {
        this.configuration = configuration;
        this.strategyFactory = strategyFactory;
        
        // 创建监控线程池
        this.monitorExecutor = Executors.newScheduledThreadPool(
                configuration.getMonitorThreadPoolSize(),
                r -> {
                    Thread t = new Thread(r, "ThreadPoolMonitor-" + System.currentTimeMillis());
                    t.setDaemon(true);
                    return t;
                }
        );
        
        // 创建异步处理线程池
        this.asyncExecutor = Executors.newFixedThreadPool(
                Math.max(2, configuration.getMonitorThreadPoolSize() / 2),
                r -> {
                    Thread t = new Thread(r, "ThreadPoolMonitor-Async-" + System.currentTimeMillis());
                    t.setDaemon(true);
                    return t;
                }
        );
        
        // 初始化默认策略
        initializeDefaultStrategies();
        
        log.info("DefaultAdvancedThreadPoolMonitor initialized with {} strategies", strategies.size());
    }
    
    /**
     * 初始化默认策略
     */
    private void initializeDefaultStrategies() {
        List<MonitorStrategy> defaultStrategies = strategyFactory.createDefaultStrategies();
        for (MonitorStrategy strategy : defaultStrategies) {
            strategies.put(strategy.getName(), strategy);
        }
    }
    
    @Override
    public RegistrationResult registerThreadPool(MonitorableThreadPool threadPool) {
        if (threadPool == null) {
            return new RegistrationResultImpl(false, "Thread pool cannot be null", "INVALID_PARAMETER");
        }
        
        String poolName = threadPool.getPoolName();
        if (poolName == null || poolName.trim().isEmpty()) {
            return new RegistrationResultImpl(false, "Thread pool name cannot be null or empty", "INVALID_NAME");
        }
        
        if (threadPool.getExecutor() == null) {
            return new RegistrationResultImpl(false, "Thread pool executor cannot be null", "INVALID_EXECUTOR");
        }
        
        // 检查是否已注册
        if (registeredPools.containsKey(poolName)) {
            return new RegistrationResultImpl(false, "Thread pool already registered: " + poolName, "ALREADY_REGISTERED");
        }
        
        try {
            registeredPools.put(poolName, threadPool);
            statistics.incrementRegisteredPools();
            
            // 为特定类型的线程池添加专用策略
            addStrategiesForThreadPool(threadPool);
            
            log.info("Successfully registered thread pool: {} (type: {})", poolName, threadPool.getPoolType());
            return new RegistrationResultImpl(true, "Thread pool registered successfully", null);
            
        } catch (Exception e) {
            log.error("Failed to register thread pool: {}", poolName, e);
            return new RegistrationResultImpl(false, "Registration failed: " + e.getMessage(), "REGISTRATION_ERROR");
        }
    }
    
    /**
     * 简化注册线程池 - 只需要名称和执行器
     */
    public RegistrationResult registerThreadPool(String name, ThreadPoolExecutor executor) {
        MonitorableThreadPool monitorablePool =
                com.konors.threadpool.monitor.core.util.ThreadPoolUtil.createMonitorablePool(name, executor);
        return registerThreadPool(monitorablePool);
    }

    /**
     * 简化注册线程池 - 带优先级
     */
    public RegistrationResult registerThreadPool(String name, ThreadPoolExecutor executor, int priority) {
        MonitorableThreadPool monitorablePool =
                com.konors.threadpool.monitor.core.util.ThreadPoolUtil.createMonitorablePool(name, executor, priority);
        return registerThreadPool(monitorablePool);
    }

    /**
     * 为线程池添加专用策略
     */
    private void addStrategiesForThreadPool(MonitorableThreadPool threadPool) {
        List<MonitorStrategy> typeSpecificStrategies = 
                strategyFactory.createStrategiesForThreadPoolType(threadPool.getPoolType());
        
        for (MonitorStrategy strategy : typeSpecificStrategies) {
            if (!strategies.containsKey(strategy.getName())) {
                strategies.put(strategy.getName(), strategy);
                log.debug("Added type-specific strategy: {} for thread pool: {}", 
                        strategy.getName(), threadPool.getPoolName());
            }
        }
    }
    
    @Override
    public boolean unregisterThreadPool(String poolName) {
        if (poolName == null || poolName.trim().isEmpty()) {
            return false;
        }
        
        MonitorableThreadPool removed = registeredPools.remove(poolName);
        if (removed != null) {
            statistics.decrementRegisteredPools();
            log.info("Successfully unregistered thread pool: {}", poolName);
            return true;
        }
        
        log.warn("Attempted to unregister non-existent thread pool: {}", poolName);
        return false;
    }
    
    @Override
    public void addMonitorStrategy(MonitorStrategy strategy) {
        if (strategy != null && strategy.getName() != null) {
            strategies.put(strategy.getName(), strategy);
            log.info("Added monitor strategy: {} (priority: {})", strategy.getName(), strategy.getPriority());
        }
    }
    
    @Override
    public boolean removeMonitorStrategy(String strategyName) {
        if (strategyName != null) {
            MonitorStrategy removed = strategies.remove(strategyName);
            if (removed != null) {
                log.info("Removed monitor strategy: {}", strategyName);
                return true;
            }
        }
        return false;
    }
    
    @Override
    public Map<String, ThreadPoolStatus> getAllThreadPoolStatus() {
        Map<String, ThreadPoolStatus> statusMap = new HashMap<>();
        
        for (Map.Entry<String, MonitorableThreadPool> entry : registeredPools.entrySet()) {
            try {
                ThreadPoolStatus status = collectThreadPoolStatus(entry.getValue());
                statusMap.put(entry.getKey(), status);
            } catch (Exception e) {
                log.error("Failed to collect status for thread pool: {}", entry.getKey(), e);
            }
        }
        
        return statusMap;
    }
    
    @Override
    public CompletableFuture<Map<String, ThreadPoolStatus>> getAllThreadPoolStatusAsync() {
        return CompletableFuture.supplyAsync(this::getAllThreadPoolStatus, asyncExecutor);
    }
    
    @Override
    public Optional<ThreadPoolStatus> getThreadPoolStatus(String poolName) {
        MonitorableThreadPool threadPool = registeredPools.get(poolName);
        if (threadPool == null) {
            return Optional.empty();
        }
        
        try {
            ThreadPoolStatus status = collectThreadPoolStatus(threadPool);
            return Optional.ofNullable(status);
        } catch (Exception e) {
            log.error("Failed to collect status for thread pool: {}", poolName, e);
            return Optional.empty();
        }
    }
    
    @Override
    public Map<String, ThreadPoolStatus> getBatchThreadPoolStatus(List<String> poolNames) {
        if (poolNames == null || poolNames.isEmpty()) {
            return new HashMap<>();
        }
        
        Map<String, ThreadPoolStatus> statusMap = new HashMap<>();
        
        for (String poolName : poolNames) {
            getThreadPoolStatus(poolName).ifPresent(status -> statusMap.put(poolName, status));
        }
        
        return statusMap;
    }
    
    @Override
    public List<String> getHealthyThreadPools() {
        return registeredPools.entrySet().stream()
                .filter(entry -> isThreadPoolHealthy(entry.getValue()))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<String> getUnhealthyThreadPools() {
        return registeredPools.entrySet().stream()
                .filter(entry -> !isThreadPoolHealthy(entry.getValue()))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<MonitorStrategy.MonitorResult> performMonitorCheck(MonitorContext context) {
        List<MonitorStrategy.MonitorResult> results = new ArrayList<>();
        
        // 按优先级排序策略
        List<MonitorStrategy> sortedStrategies = strategies.values().stream()
                .sorted(Comparator.comparingInt(MonitorStrategy::getPriority).reversed())
                .collect(Collectors.toList());
        
        for (Map.Entry<String, MonitorableThreadPool> entry : registeredPools.entrySet()) {
            MonitorableThreadPool threadPool = entry.getValue();
            
            for (MonitorStrategy strategy : sortedStrategies) {
                if (strategy.supports(threadPool)) {
                    try {
                        MonitorStrategy.MonitorResult result = strategy.monitor(threadPool, context);
                        if (result != null) {
                            results.add(result);
                            
                            if (result.shouldAlert()) {
                                statistics.incrementAlerts();
                            }
                        }
                    } catch (Exception e) {
                        log.error("Strategy {} failed for thread pool {}", 
                                strategy.getName(), threadPool.getPoolName(), e);
                    }
                }
            }
        }
        
        statistics.incrementMonitorCycles();
        statistics.updateLastMonitorTime();
        
        return results;
    }
    
    @Override
    public CompletableFuture<List<MonitorStrategy.MonitorResult>> performMonitorCheckAsync(MonitorContext context) {
        return CompletableFuture.supplyAsync(() -> performMonitorCheck(context), asyncExecutor);
    }
    
    @Override
    public MonitorStatistics getMonitorStatistics() {
        return statistics;
    }
    
    @Override
    public void startMonitoring() {
        synchronized (stateLock) {
            if (currentState == MonitoringState.RUNNING) {
                log.warn("Monitoring is already running");
                return;
            }
            
            try {
                monitoringTask = monitorExecutor.scheduleAtFixedRate(
                        this::executeMonitoringCycle,
                        0,
                        configuration.getMonitorInterval().toMillis(),
                        TimeUnit.MILLISECONDS
                );
                
                currentState = MonitoringState.RUNNING;
                log.info("Thread pool monitoring started with interval: {}", configuration.getMonitorInterval());
                
            } catch (Exception e) {
                currentState = MonitoringState.ERROR;
                log.error("Failed to start monitoring", e);
                throw new RuntimeException("Failed to start monitoring", e);
            }
        }
    }
    
    @Override
    public void stopMonitoring() {
        synchronized (stateLock) {
            if (monitoringTask != null) {
                monitoringTask.cancel(false);
                monitoringTask = null;
            }
            
            currentState = MonitoringState.STOPPED;
            log.info("Thread pool monitoring stopped");
        }
    }
    
    @Override
    public void pauseMonitoring() {
        synchronized (stateLock) {
            if (currentState == MonitoringState.RUNNING) {
                if (monitoringTask != null) {
                    monitoringTask.cancel(false);
                    monitoringTask = null;
                }
                currentState = MonitoringState.PAUSED;
                log.info("Thread pool monitoring paused");
            }
        }
    }
    
    @Override
    public void resumeMonitoring() {
        synchronized (stateLock) {
            if (currentState == MonitoringState.PAUSED) {
                startMonitoring();
                log.info("Thread pool monitoring resumed");
            }
        }
    }
    
    @Override
    public MonitoringState getMonitoringState() {
        return currentState;
    }
    
    /**
     * 执行监控周期
     */
    private void executeMonitoringCycle() {
        try {
            long startTime = System.currentTimeMillis();
            
            MonitorContext context = MonitorContext.createDefault();
            List<MonitorStrategy.MonitorResult> results = performMonitorCheck(context);
            
            // 处理监控结果
            processMonitorResults(results);
            
            long endTime = System.currentTimeMillis();
            statistics.updateMonitoringLatency(endTime - startTime);
            
        } catch (Exception e) {
            log.error("Error during monitoring cycle", e);
            currentState = MonitoringState.ERROR;
        }
    }
    
    /**
     * 处理监控结果
     */
    private void processMonitorResults(List<MonitorStrategy.MonitorResult> results) {
        for (MonitorStrategy.MonitorResult result : results) {
            if (result.shouldAlert()) {
                log.warn("Alert: {} - {}", result.getAlertLevel(), result.getMessage());
                // 这里可以集成告警系统
            } else {
                log.debug("Monitor result: {}", result.getMessage());
            }
        }
    }
    
    /**
     * 收集线程池状态
     */
    private ThreadPoolStatus collectThreadPoolStatus(MonitorableThreadPool threadPool) {
        // 这里应该调用现有的状态收集逻辑
        // 为了演示，创建一个简化的实现
        ThreadPoolStatus status = new ThreadPoolStatus();
        status.setPoolName(threadPool.getPoolName());
        status.setTimestamp(LocalDateTime.now());
        
        ThreadPoolExecutor executor = (ThreadPoolExecutor) threadPool.getExecutor();
        status.setCorePoolSize(executor.getCorePoolSize());
        status.setMaximumPoolSize(executor.getMaximumPoolSize());
        status.setActiveCount(executor.getActiveCount());
        status.setPoolSize(executor.getPoolSize());
        status.setTaskCount(executor.getTaskCount());
        status.setCompletedTaskCount(executor.getCompletedTaskCount());
        
        // 计算利用率
        double utilization = status.getMaximumPoolSize() > 0 ? 
                (double) status.getActiveCount() / status.getMaximumPoolSize() : 0.0;
        status.setUtilization(utilization);
        
        return status;
    }
    
    /**
     * 检查线程池是否健康
     */
    private boolean isThreadPoolHealthy(MonitorableThreadPool threadPool) {
        try {
            ThreadPoolExecutor executor = (ThreadPoolExecutor) threadPool.getExecutor();
            return !executor.isShutdown() && !executor.isTerminated();
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 关闭监控器
     */
    public void shutdown() {
        stopMonitoring();
        
        monitorExecutor.shutdown();
        asyncExecutor.shutdown();
        
        try {
            if (!monitorExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                monitorExecutor.shutdownNow();
            }
            if (!asyncExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                asyncExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            monitorExecutor.shutdownNow();
            asyncExecutor.shutdownNow();
        }
        
        log.info("DefaultAdvancedThreadPoolMonitor shutdown completed");
    }
    
    /**
     * 注册结果实现
     */
    private static class RegistrationResultImpl implements RegistrationResult {
        private final boolean success;
        private final String message;
        private final String errorCode;
        
        public RegistrationResultImpl(boolean success, String message, String errorCode) {
            this.success = success;
            this.message = message;
            this.errorCode = errorCode;
        }
        
        @Override
        public boolean isSuccess() {
            return success;
        }
        
        @Override
        public String getMessage() {
            return message;
        }
        
        @Override
        public Optional<String> getErrorCode() {
            return Optional.ofNullable(errorCode);
        }
    }
    
    /**
     * 监控统计实现
     */
    private static class MonitorStatisticsImpl implements MonitorStatistics {
        private volatile int totalRegisteredPools = 0;
        private volatile long totalMonitorCycles = 0;
        private volatile long totalAlerts = 0;
        private volatile LocalDateTime lastMonitorTime;
        private volatile double totalLatency = 0;
        private volatile long latencyCount = 0;
        private final Map<String, Object> extendedStats = new ConcurrentHashMap<>();
        
        @Override
        public int getTotalRegisteredPools() {
            return totalRegisteredPools;
        }
        
        @Override
        public int getActiveMonitoringPools() {
            return totalRegisteredPools; // 简化实现
        }
        
        @Override
        public int getHealthyPools() {
            return totalRegisteredPools; // 简化实现
        }
        
        @Override
        public int getUnhealthyPools() {
            return 0; // 简化实现
        }
        
        @Override
        public long getTotalMonitorCycles() {
            return totalMonitorCycles;
        }
        
        @Override
        public long getTotalAlerts() {
            return totalAlerts;
        }
        
        @Override
        public double getAverageMonitoringLatency() {
            return latencyCount > 0 ? totalLatency / latencyCount : 0.0;
        }
        
        @Override
        public LocalDateTime getLastMonitorTime() {
            return lastMonitorTime;
        }
        
        @Override
        public Map<String, Object> getExtendedStats() {
            return new HashMap<>(extendedStats);
        }
        
        public void incrementRegisteredPools() {
            totalRegisteredPools++;
        }
        
        public void decrementRegisteredPools() {
            if (totalRegisteredPools > 0) {
                totalRegisteredPools--;
            }
        }
        
        public void incrementMonitorCycles() {
            totalMonitorCycles++;
        }
        
        public void incrementAlerts() {
            totalAlerts++;
        }
        
        public void updateLastMonitorTime() {
            lastMonitorTime = LocalDateTime.now();
        }
        
        public void updateMonitoringLatency(long latency) {
            totalLatency += latency;
            latencyCount++;
        }
    }
}