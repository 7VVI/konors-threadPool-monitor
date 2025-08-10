# 高级线程池监控器使用指南

## 概述

本文档介绍了重构后的高级线程池监控系统的使用方法。新的监控系统采用了策略模式、工厂模式和建造者模式，提供了更好的扩展性、可维护性和易用性。

## 核心特性

### 1. 抽象化设计
- **MonitorableThreadPool**: 可监控线程池接口，封装线程池元数据
- **MonitorStrategy**: 监控策略接口，支持插件化监控逻辑
- **AdvancedThreadPoolMonitor**: 高级监控器接口，提供完整监控能力
- **MonitorConfiguration**: 监控配置类，支持灵活的配置管理

### 2. 策略化监控
- **利用率监控**: 监控线程池利用率，支持自定义阈值
- **队列监控**: 监控任务队列状态，支持有界和无界队列
- **拒绝任务监控**: 监控任务拒绝情况
- **健康检查**: 检查线程池健康状态
- **性能分析**: 分析线程池性能指标

### 3. 异步处理
- 支持异步监控检查
- 支持异步状态收集
- 非阻塞的监控操作

## 快速开始

### 1. 基本使用

```java
// 创建线程池
ThreadPoolExecutor executor = new ThreadPoolExecutor(
    5, 10, 60L, TimeUnit.SECONDS,
    new LinkedBlockingQueue<>(100),
    new ThreadFactoryBuilder().setNameFormat("worker-%d").build()
);

// 使用构建器创建监控器
AdvancedThreadPoolMonitor monitor = ThreadPoolMonitorBuilder
    .createDefault()
    .addThreadPool("worker-pool", executor, MonitorableThreadPool.ThreadPoolType.FIXED)
    .addUtilizationStrategy(0.8, 0.95)
    .addQueueStrategy(80, 95)
    .buildAndStart();

// 获取监控状态
Optional<ThreadPoolStatus> status = monitor.getThreadPoolStatus("worker-pool");
if (status.isPresent()) {
    System.out.println("Pool utilization: " + status.get().getUtilization());
}
```

### 2. 高级配置

```java
// 创建自定义配置
MonitorConfiguration config = MonitorConfiguration.builder()
    .monitorInterval(Duration.ofSeconds(3))
    .alertCheckInterval(Duration.ofSeconds(5))
    .maxHistoryRecords(1000)
    .adaptiveMonitoringEnabled(true)
    .predictiveAlertingEnabled(true)
    .build();

// 创建监控器
AdvancedThreadPoolMonitor monitor = new DefaultAdvancedThreadPoolMonitor(
    config, 
    new DefaultMonitorStrategyFactory()
);

// 注册线程池
MonitorableThreadPool monitorablePool = DefaultMonitorableThreadPool.builder()
    .name("custom-pool")
    .executor(executor)
    .type(MonitorableThreadPool.ThreadPoolType.CUSTOM)
    .configuration(poolConfig)
    .priority(100)
    .build();

RegistrationResult result = monitor.registerThreadPool(monitorablePool);
if (result.isSuccess()) {
    System.out.println("Thread pool registered successfully");
}
```

### 3. 自定义监控策略

```java
// 实现自定义监控策略
public class CustomMonitorStrategy implements MonitorStrategy {
    @Override
    public String getName() {
        return "CustomStrategy";
    }
    
    @Override
    public int getPriority() {
        return 50;
    }
    
    @Override
    public boolean supports(MonitorableThreadPool threadPool) {
        return threadPool.getType() == MonitorableThreadPool.ThreadPoolType.CUSTOM;
    }
    
    @Override
    public MonitorResult monitor(MonitorableThreadPool threadPool, MonitorContext context) {
        // 实现自定义监控逻辑
        return new CustomMonitorResult(...);
    }
}

// 添加自定义策略
monitor.addMonitorStrategy(new CustomMonitorStrategy());
```

## 使用场景

### 1. Web应用监控

```java
// 适用于Web应用的监控配置
AdvancedThreadPoolMonitor webMonitor = ThreadPoolMonitorBuilder
    .forWebApplication()
    .addThreadPool("http-pool", httpExecutor)
    .addThreadPool("async-pool", asyncExecutor)
    .buildAndStart();
```

### 2. 批处理应用监控

```java
// 适用于批处理应用的监控配置
AdvancedThreadPoolMonitor batchMonitor = ThreadPoolMonitorBuilder
    .forBatchApplication()
    .addThreadPool("batch-pool", batchExecutor)
    .addPerformanceStrategy(200, 120000L)
    .buildAndStart();
```

### 3. 实时应用监控

```java
// 适用于实时应用的监控配置
AdvancedThreadPoolMonitor realtimeMonitor = ThreadPoolMonitorBuilder
    .forRealtimeApplication()
    .addThreadPool("realtime-pool", realtimeExecutor)
    .withPredictiveAlerting(true)
    .buildAndStart();
```

## 监控结果处理

### 1. 同步监控检查

```java
MonitorContext context = MonitorContext.createDefault();
List<MonitorStrategy.MonitorResult> results = monitor.performMonitorCheck(context);

for (MonitorStrategy.MonitorResult result : results) {
    if (result.needsAlert()) {
        System.out.println("Alert: " + result.getMessage());
        System.out.println("Suggested Action: " + result.getSuggestedAction());
        
        // 处理告警
        handleAlert(result);
    }
}
```

### 2. 异步监控检查

```java
CompletableFuture<List<MonitorStrategy.MonitorResult>> futureResults = 
    monitor.performMonitorCheckAsync(context);

futureResults.thenAccept(results -> {
    results.stream()
        .filter(MonitorStrategy.MonitorResult::needsAlert)
        .forEach(this::handleAlert);
});
```

### 3. 批量状态获取

```java
// 获取所有线程池状态
CompletableFuture<Map<String, ThreadPoolStatus>> allStatus = 
    monitor.getAllThreadPoolStatusAsync();

allStatus.thenAccept(statusMap -> {
    statusMap.forEach((poolName, status) -> {
        System.out.printf("Pool: %s, Utilization: %.2f%%, Queue: %d%n",
            poolName, status.getUtilization() * 100, status.getQueueSize());
    });
});
```

## 扩展指南

### 1. 自定义策略工厂

```java
public class CustomStrategyFactory implements MonitorStrategyFactory {
    @Override
    public Optional<MonitorStrategy> createStrategy(StrategyType strategyType, StrategyConfig config) {
        switch (strategyType) {
            case CUSTOM:
                return Optional.of(new MyCustomStrategy(config));
            default:
                return Optional.empty();
        }
    }
    
    // 实现其他方法...
}
```

### 2. 自定义配置

```java
// 扩展配置参数
MonitorConfiguration config = MonitorConfiguration.builder()
    .extendedConfig(Map.of(
        "customParam1", "value1",
        "customParam2", 42,
        "customParam3", true
    ))
    .build();

// 在策略中使用扩展配置
String customValue = config.getExtendedConfig("customParam1", String.class, "default");
```

### 3. 业务标签

```java
// 添加业务标签
Map<String, String> businessTags = Map.of(
    "service", "user-service",
    "environment", "production",
    "version", "1.0.0"
);

MonitorableThreadPool taggedPool = DefaultMonitorableThreadPool.wrapWithTags(
    "tagged-pool", executor, ThreadPoolType.FIXED, 100, businessTags
);
```

## 性能优化

### 1. 监控频率调优

```java
// 根据应用特性调整监控频率
MonitorConfiguration config = MonitorConfiguration.builder()
    .monitorInterval(Duration.ofSeconds(5))        // 基础监控间隔
    .alertCheckInterval(Duration.ofSeconds(10))    // 告警检查间隔
    .metricsCollectionInterval(Duration.ofSeconds(3)) // 指标收集间隔
    .build();
```

### 2. 异步处理优化

```java
// 配置异步处理参数
MonitorConfiguration config = MonitorConfiguration.builder()
    .monitorThreadPoolSize(4)                     // 监控线程池大小
    .batchSize(50)                                // 批处理大小
    .asyncProcessingTimeout(Duration.ofSeconds(30)) // 异步超时
    .build();
```

### 3. 内存优化

```java
// 控制历史数据大小
MonitorConfiguration config = MonitorConfiguration.builder()
    .maxHistoryRecords(1000)                      // 最大历史记录数
    .historyRetentionPeriod(Duration.ofHours(12)) // 历史数据保留时间
    .build();
```

## 最佳实践

### 1. 命名规范
- 使用有意义的线程池名称
- 包含业务上下文信息
- 避免特殊字符和空格

### 2. 监控策略选择
- 根据线程池类型选择合适的策略
- 设置合理的告警阈值
- 避免过度监控导致性能影响

### 3. 告警处理
- 实现告警抑制机制
- 提供清晰的告警信息
- 包含可操作的建议

### 4. 资源管理
- 及时关闭监控器
- 合理配置线程池大小
- 监控监控器本身的资源使用

```java
// 应用关闭时清理资源
@PreDestroy
public void cleanup() {
    if (monitor instanceof DefaultAdvancedThreadPoolMonitor) {
        ((DefaultAdvancedThreadPoolMonitor) monitor).shutdown();
    }
}
```

## 故障排查

### 1. 常见问题

**问题**: 监控器无法启动
**解决**: 检查配置有效性，确保线程池配置正确

**问题**: 告警过于频繁
**解决**: 调整告警阈值或增加告警抑制时间

**问题**: 监控延迟过高
**解决**: 减少监控频率或增加监控线程池大小

### 2. 调试技巧

```java
// 启用详细日志
MonitorConfiguration config = MonitorConfiguration.builder()
    .extendedConfig(Map.of("logLevel", "DEBUG"))
    .build();

// 获取监控统计信息
MonitorStatistics stats = monitor.getMonitorStatistics();
System.out.println("Total cycles: " + stats.getTotalMonitorCycles());
System.out.println("Average latency: " + stats.getAverageMonitoringLatency());
```

## 总结

重构后的线程池监控系统提供了：

1. **更好的抽象**: 清晰的接口定义和职责分离
2. **更强的扩展性**: 支持自定义策略和配置
3. **更高的性能**: 异步处理和批量操作
4. **更易的使用**: 流式API和预设配置
5. **更好的维护性**: 模块化设计和标准化接口

通过这些改进，监控系统能够更好地适应不同的应用场景，提供更准确的监控数据和更及时的告警信息。