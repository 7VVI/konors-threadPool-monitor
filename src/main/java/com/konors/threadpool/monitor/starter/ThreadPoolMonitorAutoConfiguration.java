package com.konors.threadpool.monitor.starter;

import com.konors.threadpool.monitor.core.abstraction.AdvancedThreadPoolMonitor;
import com.konors.threadpool.monitor.core.abstraction.MonitorConfiguration;
import com.konors.threadpool.monitor.core.abstraction.MonitorStrategyFactory;
import com.konors.threadpool.monitor.core.factory.PropertyBasedMonitorStrategyFactory;
import com.konors.threadpool.monitor.core.impl.DefaultAdvancedThreadPoolMonitor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 线程池监控自动配置类
 * 提供默认的监控组件配置，并将配置文件中的属性应用到监控配置中
 *
 * @author zhangYh
 * @Date 2025/8/10 10:34
 */
@Configuration
@EnableScheduling
@EnableConfigurationProperties(ThreadPoolMonitorProperties.class)
public class ThreadPoolMonitorAutoConfiguration {

    /**
     * 基于配置文件属性的监控策略工厂
     */
    @Bean
    @ConditionalOnMissingBean
    public MonitorStrategyFactory monitorStrategyFactory(ThreadPoolMonitorProperties properties) {
        return new PropertyBasedMonitorStrategyFactory(properties);
    }

    @Bean
    public ExecutorService loginTaskExecutor(AdvancedThreadPoolMonitor advancedThreadPoolMonitor) {

        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        // 核心池大小
        executor.setCorePoolSize(Runtime.getRuntime().availableProcessors() / 2);
        // 最大线程数
        executor.setMaxPoolSize(Runtime.getRuntime().availableProcessors());
        // 队列容量
        executor.setQueueCapacity(100000);
        executor.setThreadPriority(Thread.MAX_PRIORITY);
        executor.setDaemon(false);
        executor.setAllowCoreThreadTimeOut(false);
        // 线程空闲时间
        executor.setKeepAliveSeconds(120);
        // 线程名字前缀
        executor.setThreadNamePrefix("login-task-executor-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        advancedThreadPoolMonitor.registerThreadPool("test", executor.getThreadPoolExecutor());
        return executor.getThreadPoolExecutor();

    }

    /**
     * 将配置文件属性映射为监控配置
     */
    @Bean
    @ConditionalOnMissingBean
    public MonitorConfiguration monitorConfiguration(ThreadPoolMonitorProperties properties) {
        return MonitorConfiguration.builder()
                .monitorInterval(Duration.ofMillis(properties.getMonitorInterval()))
                .alertCheckInterval(Duration.ofMillis(properties.getMonitorInterval()))
                .metricsCollectionInterval(Duration.ofMillis(properties.getMonitorInterval()))
                .historyRetentionPeriod(Duration.ofMillis(properties.getDataRetentionTime()))
                .maxHistoryRecords(2000)
                .adaptiveMonitoringEnabled(true)
                .predictiveAlertingEnabled(properties.isPredictiveAlertEnabled())
                .monitorThreadPoolSize(properties.getMonitorThreadPoolSize())
                .alertSuppressionPeriod(Duration.ofMillis(properties.getAlertSuppressionTime()))
                .batchSize(100)
                .asyncProcessingTimeout(Duration.ofSeconds(30))
                .build();
    }

    /**
     * 配置默认的高级线程池监控器
     */
    @Bean
    @ConditionalOnMissingBean
    public AdvancedThreadPoolMonitor advancedThreadPoolMonitor(MonitorConfiguration configuration,
                                                               MonitorStrategyFactory strategyFactory,
                                                               ThreadPoolMonitorProperties properties) {
        DefaultAdvancedThreadPoolMonitor monitor = new DefaultAdvancedThreadPoolMonitor(configuration, strategyFactory);
        if (properties.isEnabled()) {
            monitor.startMonitoring();
        }
        return monitor;
    }
}
