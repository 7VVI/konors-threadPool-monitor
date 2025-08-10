package com.konors.threadpool.monitor.core;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author zhangYh
 * @Date 2025/8/10 10:25
 * @desc
 */
@Data
public class ThreadPoolMetrics {

    private final String poolName;
    private final Deque<ThreadPoolStatus> snapshots;
    private final int maxSnapshots;

    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    // 缓存统计值，避免重复计算
    private double totalUtilization = 0.0;
    private double peakUtilization = 0.0;

    public ThreadPoolMetrics(String poolName) {
        this(poolName, 1000); // 默认最大快照数
    }

    public ThreadPoolMetrics(String poolName, int maxSnapshots) {
        this.poolName = poolName;
        this.maxSnapshots = maxSnapshots;
        this.snapshots = new ArrayDeque<>(maxSnapshots);
    }

    /**
     * 添加线程池状态快照
     */
    public void addSnapshot(ThreadPoolStatus status) {
        lock.writeLock().lock();
        try {
            if (snapshots.size() == maxSnapshots) {
                ThreadPoolStatus removed = snapshots.removeFirst();
                totalUtilization -= removed.getUtilization();
            }
            snapshots.addLast(status);
            totalUtilization += status.getUtilization();
            peakUtilization = Math.max(peakUtilization, status.getUtilization());
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * 获取所有快照（不可修改）
     */
    public List<ThreadPoolStatus> getSnapshots() {
        lock.readLock().lock();
        try {
            return Collections.unmodifiableList(new ArrayList<>(snapshots));
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * 获取最新快照
     */
    public ThreadPoolStatus getLatestSnapshot() {
        lock.readLock().lock();
        try {
            return snapshots.peekLast();
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * 获取平均利用率（O(1)）
     */
    public double getAverageUtilization() {
        lock.readLock().lock();
        try {
            return snapshots.isEmpty() ? 0.0 : totalUtilization / snapshots.size();
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * 获取峰值利用率（O(1)）
     */
    public double getPeakUtilization() {
        lock.readLock().lock();
        try {
            return peakUtilization;
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * 获取指定时间范围内的平均利用率
     */
    public double getAverageUtilizationBetween(LocalDateTime start, LocalDateTime end) {
        lock.readLock().lock();
        try {
            return snapshots.stream()
                    .filter(s -> !s.getTimestamp().isBefore(start) && !s.getTimestamp().isAfter(end))
                    .mapToDouble(ThreadPoolStatus::getUtilization)
                    .average()
                    .orElse(0.0);
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * 获取指定时间范围内的峰值利用率
     */
    public double getPeakUtilizationBetween(LocalDateTime start, LocalDateTime end) {
        lock.readLock().lock();
        try {
            return snapshots.stream()
                    .filter(s -> !s.getTimestamp().isBefore(start) && !s.getTimestamp().isAfter(end))
                    .mapToDouble(ThreadPoolStatus::getUtilization)
                    .max()
                    .orElse(0.0);
        } finally {
            lock.readLock().unlock();
        }
    }
}