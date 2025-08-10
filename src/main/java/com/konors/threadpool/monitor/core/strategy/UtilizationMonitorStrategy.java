package com.konors.threadpool.monitor.core.strategy;

import com.konors.threadpool.monitor.core.ThreadPoolStatus;
import com.konors.threadpool.monitor.core.abstraction.*;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 线程池利用率监控策略
 * 监控线程池的利用率，当超过阈值时触发告警
 * 
 * @author zhangYh
 * @Date 2025/1/20
 */
@Slf4j
public class UtilizationMonitorStrategy implements MonitorStrategy {
    
    private static final String STRATEGY_NAME = "UtilizationMonitor";
    private static final int DEFAULT_PRIORITY = 100;
    
    private final double warningThreshold;
    private final double criticalThreshold;
    private final long checkInterval;
    
    public UtilizationMonitorStrategy(MonitorStrategyFactory.StrategyConfig config) {
        this.warningThreshold = config.getParameter("warningThreshold", 0.8);
        this.criticalThreshold = config.getParameter("criticalThreshold", 0.95);
        this.checkInterval = config.getParameter("checkInterval", 5000L);
        
        log.info("UtilizationMonitorStrategy initialized with warningThreshold={}, criticalThreshold={}, checkInterval={}ms",
                warningThreshold, criticalThreshold, checkInterval);
    }
    
    @Override
    public String getName() {
        return STRATEGY_NAME;
    }

    @Override
    public String getStrategyName() {
        return "Utilization Monitor Strategy";
    }

    @Override
    public int getPriority() {
        return DEFAULT_PRIORITY;
    }
    
    @Override
    public boolean supports(MonitorableThreadPool threadPool) {
        // 支持所有类型的线程池
        return threadPool != null && threadPool.getExecutor() != null;
    }

    @Override
    public MonitorResult monitor(MonitorableThreadPool threadPool, ThreadPoolStatus status, MonitorContext context) {
        return null;
    }

    @Override
    public MonitorResult monitor(MonitorableThreadPool threadPool, MonitorContext context) {
        try {
            ThreadPoolStatus status = collectThreadPoolStatus(threadPool);
            if (status == null) {
                return createErrorResult("Failed to collect thread pool status");
            }
            
            double utilization = status.getUtilization();
            
            // 记录到上下文
            context.setTemporaryData("utilization_" + threadPool.getPoolName(), utilization);
            context.setTemporaryData("status_" + threadPool.getPoolName(), status);
            
            return analyzeUtilization(threadPool.getPoolName(), utilization, status);
            
        } catch (Exception e) {
            log.error("Error monitoring utilization for thread pool: {}", threadPool.getPoolName(), e);
            return createErrorResult("Monitoring error: " + e.getMessage());
        }
    }
    
    /**
     * 收集线程池状态
     */
    private ThreadPoolStatus collectThreadPoolStatus(MonitorableThreadPool threadPool) {
        try {
            var executor = threadPool.getExecutor();
            if (executor == null) {
                return null;
            }
            
            // 这里应该调用现有的状态收集逻辑
            // 为了演示，创建一个简化的状态对象
            ThreadPoolStatus status = new ThreadPoolStatus();
            status.setPoolName(threadPool.getPoolName());
            status.setTimestamp(LocalDateTime.now());
            
            // 获取基本指标
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
            
        } catch (Exception e) {
            log.error("Failed to collect thread pool status for: {}", threadPool.getPoolName(), e);
            return null;
        }
    }
    
    /**
     * 分析利用率并生成监控结果
     */
    private MonitorResult analyzeUtilization(String poolName, double utilization, ThreadPoolStatus status) {
        AlertLevel alertLevel;
        String message;
        String suggestedAction;
        boolean needsAlert;
        
        if (utilization >= criticalThreshold) {
            alertLevel = AlertLevel.CRITICAL;
            needsAlert = true;
            message = String.format("Thread pool '%s' utilization is critically high: %.2f%% (threshold: %.2f%%)", 
                    poolName, utilization * 100, criticalThreshold * 100);
            suggestedAction = "Consider increasing maximum pool size or optimizing task processing";
            
        } else if (utilization >= warningThreshold) {
            alertLevel = AlertLevel.WARN;
            needsAlert = true;
            message = String.format("Thread pool '%s' utilization is high: %.2f%% (threshold: %.2f%%)", 
                    poolName, utilization * 100, warningThreshold * 100);
            suggestedAction = "Monitor closely and consider scaling if trend continues";
            
        } else {
            alertLevel = AlertLevel.INFO;
            needsAlert = false;
            message = String.format("Thread pool '%s' utilization is normal: %.2f%%", 
                    poolName, utilization * 100);
            suggestedAction = "No action required";
        }
        
        // 创建扩展数据
        Map<String, Object> extendedData = new HashMap<>();
        extendedData.put("utilization", utilization);
        extendedData.put("warningThreshold", warningThreshold);
        extendedData.put("criticalThreshold", criticalThreshold);
        extendedData.put("activeCount", status.getActiveCount());
        extendedData.put("maximumPoolSize", status.getMaximumPoolSize());
        extendedData.put("poolSize", status.getPoolSize());
        
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
                "Utilization monitoring failed: " + errorMessage,
                "Check thread pool configuration and monitoring setup",
                extendedData
        );
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