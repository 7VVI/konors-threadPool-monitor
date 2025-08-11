package com.konors.threadpool.monitor.core.factory;

import com.konors.threadpool.monitor.core.abstraction.MonitorStrategy;
import com.konors.threadpool.monitor.core.abstraction.MonitorStrategyFactory;
import com.konors.threadpool.monitor.core.abstraction.DefaultStrategyConfig;
import com.konors.threadpool.monitor.core.factory.DefaultMonitorStrategyFactory;
import com.konors.threadpool.monitor.core.abstraction.MonitorStrategyFactory.StrategyType;
import com.konors.threadpool.monitor.starter.ThreadPoolMonitorProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * 基于配置文件属性的策略工厂
 * 使用 ThreadPoolMonitorProperties 中的阈值构建默认策略
 */
public class PropertyBasedMonitorStrategyFactory extends DefaultMonitorStrategyFactory implements MonitorStrategyFactory {

    private final ThreadPoolMonitorProperties properties;

    public PropertyBasedMonitorStrategyFactory(ThreadPoolMonitorProperties properties) {
        super();
        this.properties = properties;
    }

    @Override
    public List<MonitorStrategy> createDefaultStrategies() {
        List<MonitorStrategy> strategies = new ArrayList<>();

        // 利用率策略
        createStrategy(StrategyType.UTILIZATION_MONITOR,
                DefaultStrategyConfig.createUtilizationConfig(
                        properties.getDefaultUtilizationWarningThreshold(),
                        properties.getDefaultUtilizationCriticalThreshold()
                )).ifPresent(strategies::add);

        // 队列策略
        createStrategy(StrategyType.QUEUE_MONITOR,
                DefaultStrategyConfig.createQueueConfig(
                        properties.getDefaultQueueWarningThreshold(),
                        properties.getDefaultQueueCriticalThreshold()
                )).ifPresent(strategies::add);

        // 拒绝策略：沿用父类的默认
        createStrategy(StrategyType.REJECTION_MONITOR,
                DefaultStrategyConfig.createRejectionConfig(10, 50, 60000L)
        ).ifPresent(strategies::add);

        // 健康检查：沿用父类默认
        createStrategy(StrategyType.HEALTH_CHECK,
                DefaultStrategyConfig.createHealthCheckConfig()
        ).ifPresent(strategies::add);

        return strategies;
    }
}