package com.konors.threadpool.monitor.core;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author zhangYh
 * @Date 2025/8/10 10:28
 * 线程池统计数据
 * <p>
 * 用于记录某一统计周期内线程池的各项性能指标，
 * 便于趋势分析、性能优化和容量规划。
 */
@Data
public class ThreadPoolStatistics {

    /**
     * 线程池名称
     * 用于标识具体的线程池
     */
    private String poolName;

    /**
     * 统计周期开始时间
     */
    private LocalDateTime periodStart;

    /**
     * 统计周期结束时间
     */
    private LocalDateTime periodEnd;

    /**
     * 样本数
     * 在本统计周期内采集的状态快照数量
     */
    private int sampleCount;

    /**
     * 平均线程池利用率
     * (活跃线程数 / 最大线程数) 的平均值
     */
    private double avgUtilization;

    /**
     * 最大线程池利用率
     */
    private double maxUtilization;

    /**
     * 最小线程池利用率
     */
    private double minUtilization;

    /**
     * 平均队列利用率
     * (已占用队列容量 / 队列总容量) 的平均值
     */
    private double avgQueueUtilization;

    /**
     * 最大队列利用率
     */
    private double maxQueueUtilization;

    /**
     * 最小队列利用率
     */
    private double minQueueUtilization;

    /**
     * 平均活跃线程数
     */
    private double avgActiveCount;

    /**
     * 最大活跃线程数
     */
    private int maxActiveCount;

    /**
     * 最小活跃线程数
     */
    private int minActiveCount;

    /**
     * 已完成任务数增量
     * 本周期内完成的任务数
     */
    private long completedTasksDelta;
}
