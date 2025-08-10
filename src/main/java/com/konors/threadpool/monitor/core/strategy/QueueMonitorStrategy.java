package com.konors.threadpool.monitor.core.strategy;

import com.konors.threadpool.monitor.core.ThreadPoolStatus;
import com.konors.threadpool.monitor.core.abstraction.*;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 线程池队列监控策略
 * 监控线程池队列的使用情况，当队列积压过多时触发告警
 * 
 * @author zhangYh
 * @Date 2025/1/20
 */
@Slf4j
public class QueueMonitorStrategy implements MonitorStrategy {
    
    private static final String STRATEGY_NAME = "QueueMonitor";
    private static final int DEFAULT_PRIORITY = 90;
    
    private final int warningSize;
    private final int criticalSize;
    private final long checkInterval;
    private final double warningUtilizationThreshold;
    private final double criticalUtilizationThreshold;
    
    public QueueMonitorStrategy(MonitorStrategyFactory.StrategyConfig config) {
        this.warningSize = config.getParameter("warningSize", 100);
        this.criticalSize = config.getParameter("criticalSize", 500);
        this.checkInterval = config.getParameter("checkInterval", 3000L);
        this.warningUtilizationThreshold = config.getParameter("warningUtilizationThreshold", 0.7);
        this.criticalUtilizationThreshold = config.getParameter("criticalUtilizationThreshold", 0.9);
        
        log.info("QueueMonitorStrategy initialized with warningSize={}, criticalSize={}, checkInterval={}ms",
                warningSize, criticalSize, checkInterval);
    }
    
    @Override
    public String getName() {
        return STRATEGY_NAME;
    }

    @Override
    public String getStrategyName() {
        return "Queue Monitor Strategy";
    }

    @Override
    public int getPriority() {
        return DEFAULT_PRIORITY;
    }
    
    @Override
    public boolean supports(MonitorableThreadPool threadPool) {
        return threadPool != null && 
               threadPool.getExecutor() instanceof ThreadPoolExecutor;
    }

    @Override
    public MonitorResult monitor(MonitorableThreadPool threadPool, ThreadPoolStatus status, MonitorContext context) {
        return null;
    }

    @Override
    public MonitorResult monitor(MonitorableThreadPool threadPool, MonitorContext context) {
        try {
            ThreadPoolExecutor executor = (ThreadPoolExecutor) threadPool.getExecutor();
            QueueMetrics queueMetrics = collectQueueMetrics(executor);
            
            if (queueMetrics == null) {
                return createErrorResult("Failed to collect queue metrics");
            }
            
            // 记录到上下文
            context.setTemporaryData("queue_metrics_" + threadPool.getPoolName(), queueMetrics);
            
            return analyzeQueueStatus(threadPool.getPoolName(), queueMetrics);
            
        } catch (Exception e) {
            log.error("Error monitoring queue for thread pool: {}", threadPool.getPoolName(), e);
            return createErrorResult("Queue monitoring error: " + e.getMessage());
        }
    }
    
    /**
     * 收集队列指标
     */
    private QueueMetrics collectQueueMetrics(ThreadPoolExecutor executor) {
        try {
            BlockingQueue<?> queue = executor.getQueue();
            
            int currentSize = queue.size();
            int remainingCapacity = queue.remainingCapacity();
            int totalCapacity = currentSize + remainingCapacity;
            
            // 处理无界队列的情况
            boolean isUnbounded = remainingCapacity == Integer.MAX_VALUE;
            double utilization = isUnbounded ? 0.0 : 
                    (totalCapacity > 0 ? (double) currentSize / totalCapacity : 0.0);
            
            return new QueueMetrics(
                    currentSize,
                    totalCapacity,
                    remainingCapacity,
                    utilization,
                    isUnbounded,
                    queue.getClass().getSimpleName()
            );
            
        } catch (Exception e) {
            log.error("Failed to collect queue metrics", e);
            return null;
        }
    }
    
    /**
     * 分析队列状态并生成监控结果
     */
    private MonitorResult analyzeQueueStatus(String poolName, QueueMetrics metrics) {
        AlertLevel alertLevel;
        String message;
        String suggestedAction;
        boolean needsAlert;
        
        // 对于无界队列，主要关注绝对大小
        if (metrics.isUnbounded()) {
            if (metrics.getCurrentSize() >= criticalSize) {
                alertLevel = AlertLevel.CRITICAL;
                needsAlert = true;
                message = String.format("Thread pool '%s' unbounded queue size is critically high: %d (threshold: %d)", 
                        poolName, metrics.getCurrentSize(), criticalSize);
                suggestedAction = "Investigate task processing bottleneck and consider bounded queue";
                
            } else if (metrics.getCurrentSize() >= warningSize) {
                alertLevel = AlertLevel.WARN;
                needsAlert = true;
                message = String.format("Thread pool '%s' unbounded queue size is high: %d (threshold: %d)", 
                        poolName, metrics.getCurrentSize(), warningSize);
                suggestedAction = "Monitor task processing rate and queue growth trend";
                
            } else {
                alertLevel = AlertLevel.INFO;
                needsAlert = false;
                message = String.format("Thread pool '%s' unbounded queue size is normal: %d", 
                        poolName, metrics.getCurrentSize());
                suggestedAction = "No action required";
            }
        } else {
            // 对于有界队列，关注利用率和绝对大小
            boolean sizeAlert = metrics.getCurrentSize() >= criticalSize;
            boolean utilizationAlert = metrics.getUtilization() >= criticalUtilizationThreshold;
            
            if (sizeAlert || utilizationAlert) {
                alertLevel = AlertLevel.CRITICAL;
                needsAlert = true;
                message = String.format("Thread pool '%s' queue is critically full: %d/%d (%.2f%%, thresholds: size=%d, util=%.2f%%)", 
                        poolName, metrics.getCurrentSize(), metrics.getTotalCapacity(), 
                        metrics.getUtilization() * 100, criticalSize, criticalUtilizationThreshold * 100);
                suggestedAction = "Increase queue capacity or thread pool size immediately";
                
            } else if (metrics.getCurrentSize() >= warningSize || metrics.getUtilization() >= warningUtilizationThreshold) {
                alertLevel = AlertLevel.WARN;
                needsAlert = true;
                message = String.format("Thread pool '%s' queue usage is high: %d/%d (%.2f%%, thresholds: size=%d, util=%.2f%%)", 
                        poolName, metrics.getCurrentSize(), metrics.getTotalCapacity(), 
                        metrics.getUtilization() * 100, warningSize, warningUtilizationThreshold * 100);
                suggestedAction = "Consider scaling thread pool or optimizing task processing";
                
            } else {
                alertLevel = AlertLevel.INFO;
                needsAlert = false;
                message = String.format("Thread pool '%s' queue usage is normal: %d/%d (%.2f%%)", 
                        poolName, metrics.getCurrentSize(), metrics.getTotalCapacity(), 
                        metrics.getUtilization() * 100);
                suggestedAction = "No action required";
            }
        }
        
        // 创建扩展数据
        Map<String, Object> extendedData = new HashMap<>();
        extendedData.put("currentSize", metrics.getCurrentSize());
        extendedData.put("totalCapacity", metrics.getTotalCapacity());
        extendedData.put("remainingCapacity", metrics.getRemainingCapacity());
        extendedData.put("utilization", metrics.getUtilization());
        extendedData.put("isUnbounded", metrics.isUnbounded());
        extendedData.put("queueType", metrics.getQueueType());
        extendedData.put("warningSize", warningSize);
        extendedData.put("criticalSize", criticalSize);
        
        return new DefaultMonitorResult(
                needsAlert,
                alertLevel,
                message,
                suggestedAction,
                extendedData
        );
    }
    
    /**
     * 创建错误结果
     */
    private MonitorResult createErrorResult(String errorMessage) {
        Map<String, Object> extendedData = new HashMap<>();
        extendedData.put("error", errorMessage);
        extendedData.put("timestamp", LocalDateTime.now());
        
        return new DefaultMonitorResult(
                true,
                AlertLevel.ERROR,
                "Queue monitoring failed: " + errorMessage,
                "Check thread pool configuration and queue setup",
                extendedData
        );
    }
    
    /**
     * 队列指标数据类
     */
    private static class QueueMetrics {
        private final int currentSize;
        private final int totalCapacity;
        private final int remainingCapacity;
        private final double utilization;
        private final boolean isUnbounded;
        private final String queueType;
        
        public QueueMetrics(int currentSize, int totalCapacity, int remainingCapacity, 
                           double utilization, boolean isUnbounded, String queueType) {
            this.currentSize = currentSize;
            this.totalCapacity = totalCapacity;
            this.remainingCapacity = remainingCapacity;
            this.utilization = utilization;
            this.isUnbounded = isUnbounded;
            this.queueType = queueType;
        }
        
        public int getCurrentSize() { return currentSize; }
        public int getTotalCapacity() { return totalCapacity; }
        public int getRemainingCapacity() { return remainingCapacity; }
        public double getUtilization() { return utilization; }
        public boolean isUnbounded() { return isUnbounded; }
        public String getQueueType() { return queueType; }
    }
    
    /**
     * 默认监控结果实现
     */
    private static class DefaultMonitorResult implements MonitorResult {
        private final boolean needsAlert;
        private final AlertLevel alertLevel;
        private final String message;
        private final String suggestedAction;
        private final Map<String, Object> extendedData;
        
        public DefaultMonitorResult(boolean needsAlert, AlertLevel alertLevel, String message, 
                                  String suggestedAction, Map<String, Object> extendedData) {
            this.needsAlert = needsAlert;
            this.alertLevel = alertLevel;
            this.message = message;
            this.suggestedAction = suggestedAction;
            this.extendedData = extendedData != null ? new HashMap<>(extendedData) : new HashMap<>();
        }

        @Override
        public boolean shouldAlert() {
            return needsAlert;
        }

        @Override
        public AlertLevel getAlertLevel() {
            return alertLevel;
        }
        
        @Override
        public String getMessage() {
            return message;
        }
        
        @Override
        public String getSuggestedAction() {
            return suggestedAction;
        }
        
        @Override
        public Map<String, Object> getExtendedData() {
            return new HashMap<>(extendedData);
        }
        
        @Override
        public String toString() {
            return String.format("MonitorResult{needsAlert=%s, level=%s, message='%s'}", 
                    needsAlert, alertLevel, message);
        }
    }
}