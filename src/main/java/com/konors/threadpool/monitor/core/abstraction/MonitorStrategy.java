package com.konors.threadpool.monitor.core.abstraction;

import com.konors.threadpool.monitor.core.ThreadPoolStatus;

/**
 * 监控策略接口
 * 使用策略模式定义不同的监控行为，支持灵活的监控策略扩展
 * 
 * @author zhangYh
 * @Date 2025/1/20
 */
public interface MonitorStrategy {

    String getName();

    /**
     * 获取策略名称
     * @return 策略唯一标识
     */
    String getStrategyName();
    
    /**
     * 获取策略优先级
     * @return 优先级，数值越大优先级越高
     */
    int getPriority();
    
    /**
     * 判断是否支持指定的线程池
     * @param threadPool 可监控线程池
     * @return true表示支持，false表示不支持
     */
    boolean supports(MonitorableThreadPool threadPool);
    
    /**
     * 执行监控逻辑
     * @param threadPool 被监控的线程池
     * @param status 当前状态
     * @param context 监控上下文
     * @return 监控结果
     */
    MonitorResult monitor(MonitorableThreadPool threadPool, ThreadPoolStatus status, MonitorContext context);

    MonitorResult monitor(MonitorableThreadPool threadPool, MonitorContext context);

    /**
     * 监控结果
     */
    interface MonitorResult {
        
        /**
         * 是否需要告警
         */
        boolean shouldAlert();
        
        /**
         * 获取告警级别
         */
        AlertLevel getAlertLevel();
        
        /**
         * 获取监控消息
         */
        String getMessage();
        
        /**
         * 获取建议操作
         */
        String getSuggestedAction();
        
        /**
         * 获取扩展数据
         */
        java.util.Map<String, Object> getExtendedData();
    }
    
    /**
     * 告警级别
     */
    enum AlertLevel {
        /** 信息 */
        INFO(0),
        /** 警告 */
        WARN(1),
        /** 错误 */
        ERROR(2),
        /** 严重 */
        CRITICAL(3);
        
        private final int level;
        
        AlertLevel(int level) {
            this.level = level;
        }
        
        public int getLevel() {
            return level;
        }
        
        public boolean isHigherThan(AlertLevel other) {
            return this.level > other.level;
        }
    }
}