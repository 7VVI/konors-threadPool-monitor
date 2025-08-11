package com.konors.threadpool.monitor.core.abstraction;

import com.konors.threadpool.monitor.core.ThreadPoolStatus;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * 高级线程池监控器接口
 * 定义了线程池监控的核心能力，支持策略化监控和异步处理
 * 
 * @author zhangYh
 * @Date 2025/1/20
 */
public interface AdvancedThreadPoolMonitor {
    
    /**
     * 注册可监控线程池
     * @param threadPool 可监控线程池
     * @return 注册结果
     */
    RegistrationResult registerThreadPool(MonitorableThreadPool threadPool);
    
    /**
     * 简化注册线程池 - 只需要名称和执行器
     * @param name 线程池名称
     * @param executor 线程池执行器
     * @return 注册结果
     */
    default RegistrationResult registerThreadPool(String name, java.util.concurrent.ThreadPoolExecutor executor) {
        MonitorableThreadPool monitorablePool = 
                com.konors.threadpool.monitor.core.util.ThreadPoolUtil.createMonitorablePool(name, executor);
        return registerThreadPool(monitorablePool);
    }
    
    /**
     * 简化注册线程池 - 带优先级
     * @param name 线程池名称
     * @param executor 线程池执行器
     * @param priority 优先级
     * @return 注册结果
     */
    default RegistrationResult registerThreadPool(String name, 
                                                  java.util.concurrent.ThreadPoolExecutor executor, 
                                                  int priority) {
        MonitorableThreadPool monitorablePool = 
                com.konors.threadpool.monitor.core.util.ThreadPoolUtil.createMonitorablePool(name, executor, priority);
        return registerThreadPool(monitorablePool);
    }
    
    /**
     * 取消注册线程池
     * @param poolName 线程池名称
     * @return 是否成功取消注册
     */
    boolean unregisterThreadPool(String poolName);
    
    /**
     * 添加监控策略
     * @param strategy 监控策略
     */
    void addMonitorStrategy(MonitorStrategy strategy);
    
    /**
     * 移除监控策略
     * @param strategyName 策略名称
     * @return 是否成功移除
     */
    boolean removeMonitorStrategy(String strategyName);
    
    /**
     * 获取所有线程池状态
     * @return 线程池状态映射
     */
    Map<String, ThreadPoolStatus> getAllThreadPoolStatus();
    
    /**
     * 异步获取所有线程池状态
     * @return 异步结果
     */
    CompletableFuture<Map<String, ThreadPoolStatus>> getAllThreadPoolStatusAsync();
    
    /**
     * 获取指定线程池状态
     * @param poolName 线程池名称
     * @return 线程池状态，如果不存在则返回空
     */
    Optional<ThreadPoolStatus> getThreadPoolStatus(String poolName);
    
    /**
     * 批量获取线程池状态
     * @param poolNames 线程池名称列表
     * @return 状态映射
     */
    Map<String, ThreadPoolStatus> getBatchThreadPoolStatus(List<String> poolNames);
    
    /**
     * 获取健康的线程池列表
     * @return 健康线程池名称列表
     */
    List<String> getHealthyThreadPools();
    
    /**
     * 获取异常的线程池列表
     * @return 异常线程池名称列表
     */
    List<String> getUnhealthyThreadPools();
    
    /**
     * 执行监控检查
     * @param context 监控上下文
     * @return 监控结果列表
     */
    List<MonitorStrategy.MonitorResult> performMonitorCheck(MonitorContext context);
    
    /**
     * 异步执行监控检查
     * @param context 监控上下文
     * @return 异步监控结果
     */
    CompletableFuture<List<MonitorStrategy.MonitorResult>> performMonitorCheckAsync(MonitorContext context);
    
    /**
     * 获取监控统计信息
     * @return 监控统计
     */
    MonitorStatistics getMonitorStatistics();
    
    /**
     * 启动监控
     */
    void startMonitoring();
    
    /**
     * 停止监控
     */
    void stopMonitoring();
    
    /**
     * 暂停监控
     */
    void pauseMonitoring();
    
    /**
     * 恢复监控
     */
    void resumeMonitoring();
    
    /**
     * 获取监控状态
     * @return 监控状态
     */
    MonitoringState getMonitoringState();
    
    /**
     * 注册结果
     */
    interface RegistrationResult {
        boolean isSuccess();
        String getMessage();
        Optional<String> getErrorCode();
    }
    
    /**
     * 监控统计信息
     */
    interface MonitorStatistics {
        int getTotalRegisteredPools();
        int getActiveMonitoringPools();
        int getHealthyPools();
        int getUnhealthyPools();
        long getTotalMonitorCycles();
        long getTotalAlerts();
        double getAverageMonitoringLatency();
        java.time.LocalDateTime getLastMonitorTime();
        Map<String, Object> getExtendedStats();
    }
    
    /**
     * 监控状态枚举
     */
    enum MonitoringState {
        /** 未启动 */
        NOT_STARTED,
        /** 运行中 */
        RUNNING,
        /** 已暂停 */
        PAUSED,
        /** 已停止 */
        STOPPED,
        /** 错误状态 */
        ERROR
    }
}