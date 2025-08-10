package com.konors.threadpool.monitor.enums;

/**
 * @author zhangYh
 * @Date 2025/8/10 10:25
 * @desc
 */
public enum AlertType {
    HIGH_UTILIZATION("高利用率"),
    QUEUE_FULL("队列满"),
    HIGH_QUEUE_UTILIZATION("队列高利用率"),
    TASK_REJECTED("任务被拒绝"),
    TOO_MANY_ACTIVE_THREADS("过多活跃线程");

    private final String description;

    AlertType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
