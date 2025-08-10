package com.konors.threadpool.monitor.starter;

import com.konors.threadpool.monitor.core.abstraction.AdvancedThreadPoolMonitor;
import com.konors.threadpool.monitor.core.abstraction.MonitorConfiguration;
import com.konors.threadpool.monitor.core.abstraction.MonitorStrategyFactory;
import com.konors.threadpool.monitor.core.factory.DefaultMonitorStrategyFactory;
import com.konors.threadpool.monitor.core.impl.DefaultAdvancedThreadPoolMonitor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 线程池监控自动配置类
 * 提供默认的监控组件配置
 * 
 * @author zhangYh
 * @Date 2025/8/10 10:34
 */
@Configuration
@EnableScheduling
@EnableConfigurationProperties(ThreadPoolMonitorProperties.class)
public class ThreadPoolMonitorAutoConfiguration {

    /**
     * 配置默认的监控策略工厂
     */
    @Bean
    @ConditionalOnMissingBean
    public MonitorStrategyFactory monitorStrategyFactory() {
        return new DefaultMonitorStrategyFactory();
    }

    @Bean
    @ConditionalOnMissingBean
    public MonitorConfiguration threadPoolMonitorProperties() {
        return MonitorConfiguration.createDefault();
    }

    /**
     * 配置默认的高级线程池监控器
     */
    @Bean
    @ConditionalOnMissingBean
    public AdvancedThreadPoolMonitor advancedThreadPoolMonitor() {
        return new DefaultAdvancedThreadPoolMonitor(threadPoolMonitorProperties());
    }
}
