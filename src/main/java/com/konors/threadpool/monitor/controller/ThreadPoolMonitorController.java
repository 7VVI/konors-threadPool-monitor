package com.konors.threadpool.monitor.controller;

import com.konors.threadpool.monitor.common.Result;
import com.konors.threadpool.monitor.core.ThreadPoolStatus;
import com.konors.threadpool.monitor.core.abstraction.AdvancedThreadPoolMonitor;
import com.konors.threadpool.monitor.core.abstraction.AdvancedThreadPoolMonitor.MonitorStatistics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 线程池监控REST API控制器
 * 提供线程池状态查询和监控管理的API接口
 *
 * @author zhangYh
 * @Date 2025/1/20
 */
@Slf4j
@RestController
@RequestMapping("/api/threadpool/monitor")
@RequiredArgsConstructor
public class ThreadPoolMonitorController {

    private final AdvancedThreadPoolMonitor threadPoolMonitor;

    /**
     * 获取所有线程池状态
     */
    @GetMapping("/status")
    public Result<Map<String, ThreadPoolStatus>> getAllThreadPoolStatus() {
        try {
            Map<String, ThreadPoolStatus> statusMap = threadPoolMonitor.getAllThreadPoolStatus();
            return Result.success("获取线程池状态成功", statusMap);
        } catch (Exception e) {
            log.error("获取线程池状态失败", e);
            return Result.error("获取线程池状态失败: " + e.getMessage());
        }
    }

    /**
     * 获取指定线程池状态
     */
    @GetMapping("/status/{poolName}")
    public Result<ThreadPoolStatus> getThreadPoolStatus(@PathVariable String poolName) {
        try {
            Optional<ThreadPoolStatus> status = threadPoolMonitor.getThreadPoolStatus(poolName);
            if (status.isPresent()) {
                return Result.success("获取线程池状态成功", status.get());
            } else {
                return Result.notFound("线程池 '" + poolName + "' 未找到");
            }
        } catch (Exception e) {
            log.error("获取线程池状态失败: {}", poolName, e);
            return Result.error("获取线程池状态失败: " + e.getMessage());
        }
    }

    /**
     * 获取监控统计信息
     */
    @GetMapping("/statistics")
    public Result<MonitorStatistics> getMonitorStatistics() {
        try {
            MonitorStatistics statistics = threadPoolMonitor.getMonitorStatistics();
            return Result.success("获取监控统计成功", statistics);
        } catch (Exception e) {
            log.error("获取监控统计失败", e);
            return Result.error("获取监控统计失败: " + e.getMessage());
        }
    }

    /**
     * 获取已注册的线程池名称列表
     */
    @GetMapping("/pools")
    public Result<List<String>> getRegisteredPools() {
        try {
            Map<String, ThreadPoolStatus> statusMap = threadPoolMonitor.getAllThreadPoolStatus();
            List<String> poolNames = statusMap.keySet().stream().sorted().collect(Collectors.toList());
            return Result.success("获取线程池列表成功", poolNames);
        } catch (Exception e) {
            log.error("获取线程池列表失败", e);
            return Result.error("获取线程池列表失败: " + e.getMessage());
        }
    }

    /**
     * 启动监控
     */
    @PostMapping("/start")
    public Result<String> startMonitoring() {
        try {
            threadPoolMonitor.startMonitoring();
            return Result.success("监控启动成功");
        } catch (Exception e) {
            log.error("启动监控失败", e);
            return Result.error("启动监控失败: " + e.getMessage());
        }
    }

    /**
     * 停止监控
     */
    @PostMapping("/stop")
    public Result<String> stopMonitoring() {
        try {
            threadPoolMonitor.stopMonitoring();
            return Result.success("监控停止成功");
        } catch (Exception e) {
            log.error("停止监控失败", e);
            return Result.error("停止监控失败: " + e.getMessage());
        }
    }

    /**
     * 暂停监控
     */
    @PostMapping("/pause")
    public Result<String> pauseMonitoring() {
        try {
            threadPoolMonitor.pauseMonitoring();
            return Result.success("监控暂停成功");
        } catch (Exception e) {
            log.error("暂停监控失败", e);
            return Result.error("暂停监控失败: " + e.getMessage());
        }
    }

    /**
     * 恢复监控
     */
    @PostMapping("/resume")
    public Result<String> resumeMonitoring() {
        try {
            threadPoolMonitor.resumeMonitoring();
            return Result.success("监控恢复成功");
        } catch (Exception e) {
            log.error("恢复监控失败", e);
            return Result.error("恢复监控失败: " + e.getMessage());
        }
    }

    /**
     * 获取监控状态
     */
    @GetMapping("/state")
    public Result<String> getMonitoringState() {
        try {
            String state = threadPoolMonitor.getMonitoringState().name();
            return Result.success("获取监控状态成功", state);
        } catch (Exception e) {
            log.error("获取监控状态失败", e);
            return Result.error("获取监控状态失败: " + e.getMessage());
        }
    }
}