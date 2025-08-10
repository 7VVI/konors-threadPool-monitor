package com.konors.threadpool.monitor.config;

import lombok.Data;

/**
 * @author zhangYh
 * @Date 2025/8/10 10:25
 * @desc
 */
@Data
public class ThreadPoolAlertConfig {
    private boolean enabled = true;
    private double maxUtilization = 80.0; // 最大利用率阈值
    private int maxQueueSize = 1000; // 最大队列大小
    private double maxQueueUtilization = 80.0; // 最大队列利用率
    private long maxRejectedTasks = 10; // 最大拒绝任务数
    private int maxActiveThreads = -1; // 最大活跃线程数（-1表示不限制）
    private long alertInterval = 300; // 告警间隔（秒）
}