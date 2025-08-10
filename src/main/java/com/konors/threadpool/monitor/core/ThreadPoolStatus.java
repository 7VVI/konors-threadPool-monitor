package com.konors.threadpool.monitor.core;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author zhangYh
 * @Date 2025/8/10 10:24
 * 线程池运行状态数据
 * <p>
 * 用于实时记录某个线程池在某一时刻的运行情况，
 * 包含线程池配置、任务执行情况、队列使用率等指标。
 */
@Data
public class ThreadPoolStatus {

    /**
     * 线程池名称
     * 例如：orderExecutor、paymentExecutor 等
     */
    private String poolName;

    /**
     * 状态采集时间
     * 记录本次状态采集的时间点
     */
    private LocalDateTime timestamp;

    /**
     * 核心线程数
     * 核心线程在空闲时也会保留，用于提高响应速度
     */
    private int corePoolSize;

    /**
     * 最大线程数
     * 线程池允许创建的最大线程数量
     */
    private int maximumPoolSize;

    /**
     * 活跃线程数
     * 当前正在执行任务的线程数量
     */
    private int activeCount;

    /**
     * 当前线程池大小
     * 包括活跃线程和空闲线程
     */
    private int poolSize;

    /**
     * 已提交任务总数
     * 从线程池创建以来，累计提交的任务数量
     */
    private long taskCount;

    /**
     * 已完成任务总数
     * 从线程池创建以来，累计执行完成的任务数量
     */
    private long completedTaskCount;

    /**
     * 队列当前任务数
     * 等待执行的任务数量
     */
    private int queueSize;

    /**
     * 队列剩余可用容量
     * 队列还可以接收的任务数量
     */
    private int queueRemainingCapacity;

    /**
     * 线程池利用率
     * 计算公式：活跃线程数 / 最大线程数 * 100%
     * 用于评估线程池的忙碌程度
     */
    private double utilization;

    /**
     * 队列利用率
     * 计算公式：已占用队列容量 / 队列总容量 * 100%
     * 用于评估队列的饱和程度
     */
    private double queueUtilization;

    /**
     * 被拒绝的任务总数
     * 线程池因饱和或策略限制而拒绝的任务数量
     */
    private long rejectedTaskCount;
}
