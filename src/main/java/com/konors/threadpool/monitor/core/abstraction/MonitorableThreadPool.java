package com.konors.threadpool.monitor.core.abstraction;

import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 可监控线程池接口
 * 定义了线程池监控的基本契约，支持不同类型的线程池实现
 * 
 * @author zhangYh
 * @Date 2025/1/20
 */
public interface MonitorableThreadPool {
    
    /**
     * 获取线程池名称
     * @return 线程池唯一标识名称
     */
    String getPoolName();
    
    /**
     * 获取底层线程池执行器
     * @return ThreadPoolExecutor实例
     */
    ThreadPoolExecutor getExecutor();
    
    /**
     * 获取线程池类型
     * @return 线程池类型枚举
     */
    ThreadPoolType getPoolType();
    
    /**
     * 获取线程池配置信息
     * @return 配置信息对象
     */
    ThreadPoolConfiguration getConfiguration();
    
    /**
     * 检查线程池是否处于健康状态
     * @return true表示健康，false表示异常
     */
    boolean isHealthy();
    
    /**
     * 获取线程池的业务标签
     * 用于分类和过滤不同业务场景的线程池
     * @return 业务标签集合
     */
    Map<String, String> getBusinessTags();
    
    /**
     * 获取线程池优先级
     * 用于监控资源分配和告警优先级
     * @return 优先级值，数值越大优先级越高
     */
    int getPriority();
    
    /**
     * 线程池类型枚举
     */
    enum ThreadPoolType {
        /** 固定大小线程池 */
        FIXED,
        /** 缓存线程池 */
        CACHED,
        /** 单线程池 */
        SINGLE,
        /** 调度线程池 */
        SCHEDULED,
        /** 工作窃取线程池 */
        WORK_STEALING,
        /** 自定义线程池 */
        CUSTOM
    }
}