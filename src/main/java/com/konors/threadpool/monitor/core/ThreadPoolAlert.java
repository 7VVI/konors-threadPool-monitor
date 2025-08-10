package com.konors.threadpool.monitor.core;

import com.konors.threadpool.monitor.enums.AlertType;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author zhangYh
 * @Date 2025/8/10 10:26
 * @desc
 */
@Data
public class ThreadPoolAlert {
    private final String poolName;
    private final AlertType alertType;
    private final String message;
    private final ThreadPoolStatus status;
    private final LocalDateTime timestamp;

    @Override
    public String toString() {
        return String.format("[%s] %s - %s: %s",
                timestamp, poolName, alertType.getDescription(), message);
    }
}