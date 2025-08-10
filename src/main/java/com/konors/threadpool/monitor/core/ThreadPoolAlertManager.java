package com.konors.threadpool.monitor.core;

import com.konors.threadpool.monitor.config.ThreadPoolAlertConfig;
import com.konors.threadpool.monitor.enums.AlertType;
import com.konors.threadpool.monitor.strategy.AlertHandler;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author zhangYh
 * @Date 2025/8/10 10:25
 * @desc
 */
@Slf4j
public class ThreadPoolAlertManager {

    private final Map<String, ThreadPoolAlertConfig> alertConfigs = new ConcurrentHashMap<>();
    private final Map<String, Map<AlertType, LocalDateTime>> lastAlertTimes = new ConcurrentHashMap<>();
    private final List<AlertHandler> alertHandlers = new ArrayList<>();

    @Resource
    private ApplicationEventPublisher eventPublisher;

    /**
     * 配置线程池告警规则
     */
    public void configureAlert(String poolName, ThreadPoolAlertConfig config) {
        alertConfigs.put(poolName, config);
        lastAlertTimes.put(poolName, new HashMap<>());
        log.info("配置线程池告警规则: {} -> {}", poolName, config);
    }

    /**
     * 添加告警处理器
     */
    public void addAlertHandler(AlertHandler handler) {
        alertHandlers.add(handler);
    }

    /**
     * 检查并触发告警
     */
    public void checkAndAlert(String poolName, ThreadPoolStatus status) {
        ThreadPoolAlertConfig config = alertConfigs.get(poolName);
        if (config == null || !config.isEnabled()) {
            return;
        }

        // 检查各种告警条件
        checkUtilizationAlert(poolName, status, config);
        checkQueueAlert(poolName, status, config);
        checkRejectedTaskAlert(poolName, status, config);
        checkActiveThreadAlert(poolName, status, config);
    }

    private void checkUtilizationAlert(String poolName, ThreadPoolStatus status, ThreadPoolAlertConfig config) {
        if (config.getMaxUtilization() > 0 && status.getUtilization() > config.getMaxUtilization()) {
            triggerAlert(poolName, AlertType.HIGH_UTILIZATION,
                    String.format("线程池利用率过高: %.2f%% (阈值: %.2f%%)",
                            status.getUtilization(), config.getMaxUtilization()),
                    status, config.getAlertInterval());
        }
    }

    private void checkQueueAlert(String poolName, ThreadPoolStatus status, ThreadPoolAlertConfig config) {
        if (config.getMaxQueueSize() > 0 && status.getQueueSize() > config.getMaxQueueSize()) {
            triggerAlert(poolName, AlertType.QUEUE_FULL,
                    String.format("队列任务过多: %d (阈值: %d)",
                            status.getQueueSize(), config.getMaxQueueSize()),
                    status, config.getAlertInterval());
        }

        if (config.getMaxQueueUtilization() > 0 && status.getQueueUtilization() > config.getMaxQueueUtilization()) {
            triggerAlert(poolName, AlertType.HIGH_QUEUE_UTILIZATION,
                    String.format("队列利用率过高: %.2f%% (阈值: %.2f%%)",
                            status.getQueueUtilization(), config.getMaxQueueUtilization()),
                    status, config.getAlertInterval());
        }
    }

    private void checkRejectedTaskAlert(String poolName, ThreadPoolStatus status, ThreadPoolAlertConfig config) {
        if (config.getMaxRejectedTasks() > 0 && status.getRejectedTaskCount() > config.getMaxRejectedTasks()) {
            triggerAlert(poolName, AlertType.TASK_REJECTED,
                    String.format("拒绝任务过多: %d (阈值: %d)",
                            status.getRejectedTaskCount(), config.getMaxRejectedTasks()),
                    status, config.getAlertInterval());
        }
    }

    private void checkActiveThreadAlert(String poolName, ThreadPoolStatus status, ThreadPoolAlertConfig config) {
        if (config.getMaxActiveThreads() > 0 && status.getActiveCount() > config.getMaxActiveThreads()) {
            triggerAlert(poolName, AlertType.TOO_MANY_ACTIVE_THREADS,
                    String.format("活跃线程过多: %d (阈值: %d)",
                            status.getActiveCount(), config.getMaxActiveThreads()),
                    status, config.getAlertInterval());
        }
    }

    private void triggerAlert(String poolName, AlertType alertType, String message,
                              ThreadPoolStatus status, long alertInterval) {

        // 检查告警间隔
        Map<AlertType, LocalDateTime> poolAlerts = lastAlertTimes.get(poolName);
        LocalDateTime lastAlertTime = poolAlerts.get(alertType);
        LocalDateTime now = LocalDateTime.now();

        if (lastAlertTime != null &&
                lastAlertTime.plusSeconds(alertInterval).isAfter(now)) {
            return; // 还在告警间隔内，不重复告警
        }

        // 记录告警时间
        poolAlerts.put(alertType, now);

        // 创建告警事件
        ThreadPoolAlert alert = new ThreadPoolAlert(poolName, alertType, message, status, now);

        // 通知所有告警处理器
        for (AlertHandler handler : alertHandlers) {
            try {
                handler.handleAlert(alert);
            } catch (Exception e) {
                log.error("处理告警失败: {}", e.getMessage(), e);
            }
        }

        // 发布Spring事件
        eventPublisher.publishEvent(alert);

        log.warn("线程池告警: {}", alert);
    }
}