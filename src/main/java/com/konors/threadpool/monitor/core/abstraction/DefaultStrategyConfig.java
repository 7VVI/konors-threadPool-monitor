package com.konors.threadpool.monitor.core.abstraction;

import lombok.Data;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 默认策略配置实现
 * 
 * @author zhangYh
 * @Date 2025/1/20
 */
@Data
public class DefaultStrategyConfig implements MonitorStrategyFactory.StrategyConfig {
    
    private final Map<String, Object> parameters = new ConcurrentHashMap<>();
    
    public DefaultStrategyConfig() {}
    
    public DefaultStrategyConfig(Map<String, Object> initialParameters) {
        if (initialParameters != null) {
            this.parameters.putAll(initialParameters);
        }
    }
    
    @Override
    public Object getParameter(String key) {
        return parameters.get(key);
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public <T> T getParameter(String key, T defaultValue) {
        Object value = parameters.get(key);
        if (value != null && defaultValue != null && defaultValue.getClass().isInstance(value)) {
            return (T) value;
        }
        return defaultValue;
    }
    
    @Override
    public void setParameter(String key, Object value) {
        if (key != null) {
            if (value != null) {
                parameters.put(key, value);
            } else {
                parameters.remove(key);
            }
        }
    }
    
    @Override
    public Set<String> getParameterKeys() {
        return parameters.keySet();
    }
    
    @Override
    public boolean isValid() {
        // 基本验证：检查必需的参数
        return parameters != null;
    }
    
    /**
     * 创建利用率监控配置
     */
    public static DefaultStrategyConfig createUtilizationConfig(double warningThreshold, double criticalThreshold) {
        DefaultStrategyConfig config = new DefaultStrategyConfig();
        config.setParameter("warningThreshold", warningThreshold);
        config.setParameter("criticalThreshold", criticalThreshold);
        config.setParameter("checkInterval", 5000L); // 5秒
        return config;
    }
    
    /**
     * 创建队列监控配置
     */
    public static DefaultStrategyConfig createQueueConfig(int warningSize, int criticalSize) {
        DefaultStrategyConfig config = new DefaultStrategyConfig();
        config.setParameter("warningSize", warningSize);
        config.setParameter("criticalSize", criticalSize);
        config.setParameter("checkInterval", 3000L); // 3秒
        return config;
    }
    
    /**
     * 创建拒绝任务监控配置
     */
    public static DefaultStrategyConfig createRejectionConfig(int warningCount, int criticalCount, long timeWindow) {
        DefaultStrategyConfig config = new DefaultStrategyConfig();
        config.setParameter("warningCount", warningCount);
        config.setParameter("criticalCount", criticalCount);
        config.setParameter("timeWindow", timeWindow);
        config.setParameter("checkInterval", 10000L); // 10秒
        return config;
    }
    
    /**
     * 创建健康检查配置
     */
    public static DefaultStrategyConfig createHealthCheckConfig() {
        DefaultStrategyConfig config = new DefaultStrategyConfig();
        config.setParameter("checkInterval", 30000L); // 30秒
        config.setParameter("timeoutThreshold", 5000L); // 5秒超时
        config.setParameter("enableDeepCheck", true);
        return config;
    }
    
    /**
     * 创建性能分析配置
     */
    public static DefaultStrategyConfig createPerformanceConfig(int sampleSize, long analysisInterval) {
        DefaultStrategyConfig config = new DefaultStrategyConfig();
        config.setParameter("sampleSize", sampleSize);
        config.setParameter("analysisInterval", analysisInterval);
        config.setParameter("enableTrendAnalysis", true);
        config.setParameter("enableBottleneckDetection", true);
        return config;
    }
    
    /**
     * 获取整数参数
     */
    public int getIntParameter(String key, int defaultValue) {
        return getParameter(key, defaultValue);
    }
    
    /**
     * 获取长整数参数
     */
    public long getLongParameter(String key, long defaultValue) {
        return getParameter(key, defaultValue);
    }
    
    /**
     * 获取双精度参数
     */
    public double getDoubleParameter(String key, double defaultValue) {
        return getParameter(key, defaultValue);
    }
    
    /**
     * 获取布尔参数
     */
    public boolean getBooleanParameter(String key, boolean defaultValue) {
        return getParameter(key, defaultValue);
    }
    
    /**
     * 获取字符串参数
     */
    public String getStringParameter(String key, String defaultValue) {
        return getParameter(key, defaultValue);
    }
    
    /**
     * 复制配置
     */
    public DefaultStrategyConfig copy() {
        return new DefaultStrategyConfig(this.parameters);
    }
    
    /**
     * 合并配置
     */
    public DefaultStrategyConfig merge(DefaultStrategyConfig other) {
        if (other != null) {
            this.parameters.putAll(other.parameters);
        }
        return this;
    }
    
    @Override
    public String toString() {
        return "DefaultStrategyConfig{" +
                "parameters=" + parameters +
                '}';
    }
}