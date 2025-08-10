package com.konors.threadpool.monitor.controller;

import com.konors.threadpool.monitor.common.BusinessException;
import com.konors.threadpool.monitor.common.Result;
import com.konors.threadpool.monitor.config.ThreadPoolAlertConfig;
import com.konors.threadpool.monitor.core.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author zhangYh
 * @Date 2025/8/10 10:43
 * @desc
 */
@Slf4j
@RestController
@RequestMapping("/api/threadpool")
public class ThreadPoolMonitorController {

    @Autowired
    private ThreadPoolMonitor threadPoolMonitor;

    @Autowired
    private ThreadPoolMetricsCollector metricsCollector;

    @Autowired
    private ThreadPoolAlertManager alertManager;

    /**
     * 获取所有线程池状态
     */
    @GetMapping("/status")
    public Result<Map<String, ThreadPoolStatus>> getAllStatus() {
        try {
            Map<String, ThreadPoolStatus> status = threadPoolMonitor.getAllThreadPoolStatus();
            return Result.success("获取线程池状态成功", status);
        } catch (Exception e) {
            log.error("获取线程池状态失败", e);
            throw BusinessException.metricsError("获取线程池状态失败: " + e.getMessage());
        }
    }

    /**
     * 获取指定线程池状态
     */
    @GetMapping("/status/{poolName}")
    public Result<ThreadPoolStatus> getStatus(@PathVariable String poolName) {
        ThreadPoolStatus status = threadPoolMonitor.getThreadPoolStatus(poolName);
        if (status == null) {
            throw BusinessException.threadPoolNotFound(poolName);
        }
        return Result.success("获取线程池状态成功", status);
    }

    /**
     * 获取线程池历史指标
     */
    @GetMapping("/metrics/{poolName}")
    public Result<List<ThreadPoolMetric>> getMetrics(
            @PathVariable String poolName,
            @RequestParam(defaultValue = "100") int count) {
        
        if (count <= 0 || count > 1000) {
            throw BusinessException.configError("查询数量必须在1-1000之间");
        }
        
        try {
            List<ThreadPoolMetric> metrics = metricsCollector.getRecentMetrics(poolName, count);
            return Result.success("获取线程池指标成功", metrics);
        } catch (Exception e) {
            log.error("获取线程池指标失败: poolName={}, count={}", poolName, count, e);
            throw BusinessException.metricsError("获取线程池指标失败: " + e.getMessage());
        }
    }

    /**
     * 获取线程池统计信息
     */
    @GetMapping("/statistics/{poolName}")
    public Result<ThreadPoolStatistics> getStatistics(
            @PathVariable String poolName,
            @RequestParam String startTime,
            @RequestParam String endTime) {

        try {
            LocalDateTime start = LocalDateTime.parse(startTime);
            LocalDateTime end = LocalDateTime.parse(endTime);
            
            // 验证时间范围
            if (start.isAfter(end)) {
                throw BusinessException.configError("开始时间不能晚于结束时间");
            }
            
            if (start.isAfter(LocalDateTime.now())) {
                throw BusinessException.configError("开始时间不能是未来时间");
            }

            ThreadPoolStatistics stats = metricsCollector.calculateStatistics(poolName, start, end);
            if (stats == null) {
                throw BusinessException.threadPoolNotFound(poolName);
            }

            return Result.success("获取线程池统计信息成功", stats);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("获取线程池统计信息失败: poolName={}, startTime={}, endTime={}", poolName, startTime, endTime, e);
            throw BusinessException.metricsError("获取线程池统计信息失败: " + e.getMessage());
        }
    }

    /**
     * 配置告警规则
     */
    @PostMapping("/alert/{poolName}")
    public Result<String> configureAlert(
            @PathVariable String poolName,
            @RequestBody ThreadPoolAlertConfig config) {
        
        if (config == null) {
            throw BusinessException.alertConfigError("告警配置不能为空");
        }
        
        try {
            alertManager.configureAlert(poolName, config);
            return Result.success("告警配置已更新");
        } catch (Exception e) {
            log.error("配置告警规则失败: poolName={}, config={}", poolName, config, e);
            throw BusinessException.alertConfigError("配置告警规则失败: " + e.getMessage());
        }
    }

    /**
     * 健康检查端点
     */
    @GetMapping("/health")
    public Result<Map<String, Object>> health() {
        try {
            Map<String, Object> health = new HashMap<>();
            Map<String, ThreadPoolStatus> allStatus = threadPoolMonitor.getAllThreadPoolStatus();

            health.put("status", "UP");
            health.put("timestamp", LocalDateTime.now());
            health.put("monitoredPools", allStatus.keySet());

            // 计算整体健康度
            long unhealthyPools = allStatus.values().stream()
                    .mapToLong(status -> status.getUtilization() > 90 ? 1 : 0)
                    .sum();

            health.put("totalPools", allStatus.size());
            health.put("unhealthyPools", unhealthyPools);
            health.put("healthScore", allStatus.isEmpty() ? 100 :
                    (double)(allStatus.size() - unhealthyPools) / allStatus.size() * 100);

            return Result.success("健康检查完成", health);
        } catch (Exception e) {
            log.error("健康检查失败", e);
            throw BusinessException.metricsError("健康检查失败: " + e.getMessage());
        }
    }
}
