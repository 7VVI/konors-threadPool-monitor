package com.konors.threadpool.monitor.starter;

import com.konors.threadpool.monitor.core.ThreadPoolAlertManager;
import com.konors.threadpool.monitor.core.ThreadPoolMonitor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author zhangYh
 * @Date 2025/8/10 10:34
 * @desc
 */
@Configuration
public class ThreadPoolMonitorAutoConfiguration {

    @Bean
    public ThreadPoolAlertManager threadPoolAlertManager(){
        return new ThreadPoolAlertManager();
    }

    @Bean
    public ThreadPoolMonitor threadPoolMonitor(){
        return new ThreadPoolMonitor();
    }
}
