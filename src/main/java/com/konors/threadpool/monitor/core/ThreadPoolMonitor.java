package com.konors.threadpool.monitor.core;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author zhangYh
 * @Date 2025/8/10 10:24
 * @desc
 */
@Slf4j
public class ThreadPoolMonitor {

    private final Map<String, ThreadPoolExecutor> monitoredPools = new ConcurrentHashMap<>();
    private final Map<String, ThreadPoolMetrics> poolMetrics = new ConcurrentHashMap<>();
    private final ScheduledExecutorService monitorExecutor = Executors.newScheduledThreadPool(2);
    private final List<ThreadPoolListener> listeners = new ArrayList<>();

    @Resource
    private ThreadPoolAlertManager alertManager;

    @Resource
    private ThreadPoolMetricsCollector metricsCollector;

    /**
     * 注册线程池进行监控
     */
    public void registerThreadPool(String poolName, ThreadPoolExecutor threadPool) {
        monitoredPools.put(poolName, threadPool);
        poolMetrics.put(poolName, new ThreadPoolMetrics(poolName));
        log.info("注册线程池监控: {}", poolName);
    }

    /**
     * 取消线程池监控
     */
    public void unregisterThreadPool(String poolName) {
        monitoredPools.remove(poolName);
        poolMetrics.remove(poolName);
        log.info("取消线程池监控: {}", poolName);
    }

    /**
     * 添加监控监听器
     */
    public void addListener(ThreadPoolListener listener) {
        listeners.add(listener);
    }

    /**
     * 获取所有线程池状态
     */
    public Map<String, ThreadPoolStatus> getAllThreadPoolStatus() {
        Map<String, ThreadPoolStatus> statusMap = new HashMap<>();

        monitoredPools.forEach((name, pool) -> {
            ThreadPoolStatus status = collectThreadPoolStatus(name, pool);
            statusMap.put(name, status);
        });

        return statusMap;
    }

    /**
     * 获取指定线程池状态
     */
    public ThreadPoolStatus getThreadPoolStatus(String poolName) {
        ThreadPoolExecutor pool = monitoredPools.get(poolName);
        if (pool == null) {
            return null;
        }
        return collectThreadPoolStatus(poolName, pool);
    }

    /**
     * 获取线程池历史指标
     */
    public ThreadPoolMetrics getThreadPoolMetrics(String poolName) {
        return poolMetrics.get(poolName);
    }

    @PostConstruct
    public void startMonitoring() {
        // 每5秒收集一次指标
        monitorExecutor.scheduleAtFixedRate(this::collectMetrics, 0, 5, TimeUnit.SECONDS);

        // 每10秒检查一次告警
        monitorExecutor.scheduleAtFixedRate(this::checkAlerts, 0, 10, TimeUnit.SECONDS);

        log.info("线程池监控已启动");
    }

    @PreDestroy
    public void stopMonitoring() {
        monitorExecutor.shutdown();
        try {
            if (!monitorExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                monitorExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            monitorExecutor.shutdownNow();
        }
        log.info("线程池监控已停止");
    }

    private void collectMetrics() {
        monitoredPools.forEach((name, pool) -> {
            ThreadPoolStatus status = collectThreadPoolStatus(name, pool);
            ThreadPoolMetrics metrics = poolMetrics.get(name);
            if (metrics != null) {
                metrics.addSnapshot(status);
                metricsCollector.collect(name, status);
            }

            // 通知监听器
            notifyListeners(name, status);
        });
    }

    private void checkAlerts() {
        monitoredPools.forEach((name, pool) -> {
            ThreadPoolStatus status = collectThreadPoolStatus(name, pool);
            alertManager.checkAndAlert(name, status);
        });
    }

    private ThreadPoolStatus collectThreadPoolStatus(String poolName, ThreadPoolExecutor pool) {
        ThreadPoolStatus status = new ThreadPoolStatus();
        status.setPoolName(poolName);
        status.setTimestamp(LocalDateTime.now());
        status.setCorePoolSize(pool.getCorePoolSize());
        status.setMaximumPoolSize(pool.getMaximumPoolSize());
        status.setActiveCount(pool.getActiveCount());
        status.setPoolSize(pool.getPoolSize());
        status.setTaskCount(pool.getTaskCount());
        status.setCompletedTaskCount(pool.getCompletedTaskCount());
        status.setQueueSize(pool.getQueue().size());
        status.setQueueRemainingCapacity(pool.getQueue().remainingCapacity());

        // 计算衍生指标
        status.setUtilization((double) status.getActiveCount() / status.getMaximumPoolSize() * 100);
        status.setQueueUtilization(calculateQueueUtilization(pool.getQueue()));
        status.setRejectedTaskCount(getRejectedTaskCount(pool));

        return status;
    }

    private double calculateQueueUtilization(BlockingQueue<?> queue) {
        if (queue instanceof LinkedBlockingQueue) {
            // LinkedBlockingQueue默认容量为Integer.MAX_VALUE
            int capacity = queue.size() + queue.remainingCapacity();
            if (capacity == Integer.MAX_VALUE) {
                return 0.0; // 无界队列
            }
            return (double) queue.size() / capacity * 100;
        }

        int totalCapacity = queue.size() + queue.remainingCapacity();
        return totalCapacity > 0 ? (double) queue.size() / totalCapacity * 100 : 0.0;
    }

    private long getRejectedTaskCount(ThreadPoolExecutor pool) {
        // 通过反射获取拒绝任务数（如果有自定义RejectedExecutionHandler）
        try {
            return ((AtomicLong) pool.getClass()
                    .getDeclaredField("rejectedTaskCount")
                    .get(pool)).get();
        } catch (Exception e) {
            // 如果无法获取，返回0
            return 0;
        }
    }

    private void notifyListeners(String poolName, ThreadPoolStatus status) {
        for (ThreadPoolListener listener : listeners) {
            try {
                listener.onStatusUpdate(poolName, status);
            } catch (Exception e) {
                log.warn("通知监听器失败: {}", e.getMessage());
            }
        }
    }
}
