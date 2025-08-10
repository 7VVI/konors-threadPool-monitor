package com.konors.threadpool.monitor.strategy;

/**
 * @author zhangYh
 * @Date 2025/8/10 10:26
 * @desc
 */

import com.konors.threadpool.monitor.core.ThreadPoolAlert;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 默认日志告警处理器
 */
@Slf4j
@Component
class LogAlertHandler implements AlertHandler {

    @Override
    public void handleAlert(ThreadPoolAlert alert) {
        log.warn("=== 线程池告警 ===");
        log.warn("线程池: {}", alert.getPoolName());
        log.warn("告警类型: {}", alert.getAlertType().getDescription());
        log.warn("告警信息: {}", alert.getMessage());
        log.warn("告警时间: {}", alert.getTimestamp());
        log.warn("当前状态: 活跃线程={}, 队列大小={}, 利用率={:.2f}%",
                alert.getStatus().getActiveCount(),
                alert.getStatus().getQueueSize(),
                alert.getStatus().getUtilization());
        log.warn("==================");
    }
}