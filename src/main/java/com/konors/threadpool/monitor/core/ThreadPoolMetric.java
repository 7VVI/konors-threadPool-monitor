package com.konors.threadpool.monitor.core;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author zhangYh
 * @Date 2025/8/10 10:28
 * @desc
 */
@Data
public class ThreadPoolMetric {

    /**
     * 线程池名称
     * 例如：orderExecutor、paymentExecutor 等
     */
    private String poolName;

    /**
     * 采集时间戳
     * 记录当前指标采集的时间
     */
    private LocalDateTime timestamp;

    /**
     * 线程池利用率
     * = 活跃线程数 / 最大线程数
     * 取值范围：0.0 ~ 1.0
     */
    private double utilization;

    /**
     * 队列利用率
     * = 当前队列任务数 / 队列最大容量
     * 取值范围：0.0 ~ 1.0
     */
    private double queueUtilization;

    /**
     * 当前活跃线程数
     * 处于执行任务状态的线程数量
     */
    private int activeCount;

    /**
     * 当前队列任务数
     * 等待执行的任务数量
     */
    private int queueSize;

    /**
     * 已完成任务总数
     * 从线程池创建开始累计完成的任务数量
     */
    private long completedTaskCount;

    /**
     * 被拒绝的任务总数
     * 任务因线程池饱和而被拒绝的次数
     */
    private long rejectedTaskCount;
}