package com.konors.threadpool.monitor.strategy;

/**
 * @author zhangYh
 * @Date 2025/8/10 10:26
 * @desc
 */

import com.konors.threadpool.monitor.core.ThreadPoolAlert;

/**
 * 告警处理器接口
 */
public interface AlertHandler {
    void handleAlert(ThreadPoolAlert alert);
}