package com.konors.threadpool.monitor.core;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @author zhangYh
 * @Date 2025/8/10 10:28
 * @desc
 */
@Slf4j
@Component
public class ThreadPoolMetricsCollector {

    private final Map<String, List<ThreadPoolMetric>> metricsHistory = new ConcurrentHashMap<>();
    private final int maxHistorySize = 2000; // 最多保存2000条历史记录

    /**
     * 收集指标数据
     */
    public void collect(String poolName, ThreadPoolStatus status) {
        ThreadPoolMetric metric = new ThreadPoolMetric();
        metric.setPoolName(poolName);
        metric.setTimestamp(status.getTimestamp());
        metric.setUtilization(status.getUtilization());
        metric.setQueueUtilization(status.getQueueUtilization());
        metric.setActiveCount(status.getActiveCount());
        metric.setQueueSize(status.getQueueSize());
        metric.setCompletedTaskCount(status.getCompletedTaskCount());
        metric.setRejectedTaskCount(status.getRejectedTaskCount());

        // 存储指标
        metricsHistory.computeIfAbsent(poolName, k -> new ArrayList<>()).add(metric);

        // 清理过期数据
        List<ThreadPoolMetric> metrics = metricsHistory.get(poolName);
        if (metrics.size() > maxHistorySize) {
            metrics.subList(0, metrics.size() - maxHistorySize).clear();
        }
    }

    /**
     * 获取指定时间范围的指标
     */
    public List<ThreadPoolMetric> getMetrics(String poolName, LocalDateTime start, LocalDateTime end) {
        List<ThreadPoolMetric> metrics = metricsHistory.get(poolName);
        if (metrics == null) {
            return Collections.emptyList();
        }

        return metrics.stream()
                .filter(m -> m.getTimestamp().isAfter(start) && m.getTimestamp().isBefore(end))
                .collect(Collectors.toList());
    }

    /**
     * 获取最近N条指标
     */
    public List<ThreadPoolMetric> getRecentMetrics(String poolName, int count) {
        List<ThreadPoolMetric> metrics = metricsHistory.get(poolName);
        if (metrics == null || metrics.isEmpty()) {
            return Collections.emptyList();
        }

        int size = metrics.size();
        int fromIndex = Math.max(0, size - count);
        return new ArrayList<>(metrics.subList(fromIndex, size));
    }

    /**
     * 计算统计信息
     */
    public ThreadPoolStatistics calculateStatistics(String poolName, LocalDateTime start, LocalDateTime end) {
        List<ThreadPoolMetric> metrics = getMetrics(poolName, start, end);
        if (metrics.isEmpty()) {
            return null;
        }

        ThreadPoolStatistics stats = new ThreadPoolStatistics();
        stats.setPoolName(poolName);
        stats.setPeriodStart(start);
        stats.setPeriodEnd(end);
        stats.setSampleCount(metrics.size());

        // 计算各种统计值
        DoubleSummaryStatistics utilizationStats = metrics.stream()
                .mapToDouble(ThreadPoolMetric::getUtilization)
                .summaryStatistics();

        DoubleSummaryStatistics queueStats = metrics.stream()
                .mapToDouble(ThreadPoolMetric::getQueueUtilization)
                .summaryStatistics();

        IntSummaryStatistics activeStats = metrics.stream()
                .mapToInt(ThreadPoolMetric::getActiveCount)
                .summaryStatistics();

        stats.setAvgUtilization(utilizationStats.getAverage());
        stats.setMaxUtilization(utilizationStats.getMax());
        stats.setMinUtilization(utilizationStats.getMin());

        stats.setAvgQueueUtilization(queueStats.getAverage());
        stats.setMaxQueueUtilization(queueStats.getMax());
        stats.setMinQueueUtilization(queueStats.getMin());

        stats.setAvgActiveCount(activeStats.getAverage());
        stats.setMaxActiveCount(activeStats.getMax());
        stats.setMinActiveCount(activeStats.getMin());

        // 计算总完成任务数变化
        if (metrics.size() > 1) {
            long firstCompleted = metrics.get(0).getCompletedTaskCount();
            long lastCompleted = metrics.get(metrics.size() - 1).getCompletedTaskCount();
            stats.setCompletedTasksDelta(lastCompleted - firstCompleted);
        }

        return stats;
    }
}