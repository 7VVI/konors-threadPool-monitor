# Konors ThreadPool Monitor - 高级线程池监控系统

[![Java](https://img.shields.io/badge/Java-11+-orange.svg)](https://www.oracle.com/java/)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)
[![Build Status](https://img.shields.io/badge/Build-Passing-green.svg)](#)

## 项目概述

Konors ThreadPool Monitor 是一个企业级的Java线程池监控系统，采用现代化的设计模式和架构，提供全面的线程池监控、告警和分析能力。

### 核心特性

- 🎯 **策略化监控**: 支持多种监控策略，可插拔式架构
- 🔧 **高度可扩展**: 基于接口设计，支持自定义策略和配置
- ⚡ **异步处理**: 非阻塞监控，支持高并发场景
- 📊 **丰富指标**: 提供利用率、队列状态、性能等多维度指标
- 🚨 **智能告警**: 多级别告警机制，支持自定义阈值
- 🏗️ **建造者模式**: 流式API，简化配置和使用
- 📈 **实时监控**: 支持实时状态查询和历史数据分析

## 架构设计

### 核心组件

```
┌─────────────────────────────────────────────────────────────┐
│                    应用层 (Application Layer)                │
├─────────────────────────────────────────────────────────────┤
│  ThreadPoolMonitorBuilder  │  集成测试  │  使用示例         │
├─────────────────────────────────────────────────────────────┤
│                    监控层 (Monitor Layer)                   │
├─────────────────────────────────────────────────────────────┤
│  AdvancedThreadPoolMonitor │ MonitorContext │ MonitorConfig  │
├─────────────────────────────────────────────────────────────┤
│                    策略层 (Strategy Layer)                  │
├─────────────────────────────────────────────────────────────┤
│  MonitorStrategy │ UtilizationStrategy │ QueueStrategy      │
├─────────────────────────────────────────────────────────────┤
│                    工厂层 (Factory Layer)                   │
├─────────────────────────────────────────────────────────────┤
│  MonitorStrategyFactory │ DefaultStrategyConfig             │
├─────────────────────────────────────────────────────────────┤
│                    核心层 (Core Layer)                      │
├─────────────────────────────────────────────────────────────┤
│  MonitorableThreadPool │ ThreadPoolStatus │ ThreadPoolMetrics│
└─────────────────────────────────────────────────────────────┘
```

### 设计模式

- **策略模式**: 监控策略的可插拔实现
- **工厂模式**: 策略和监控器的创建管理
- **建造者模式**: 复杂对象的构建
- **装饰器模式**: 线程池的监控能力增强
- **观察者模式**: 监控事件的通知机制

## 快速开始

### 1. 基本使用

```java
// 创建线程池
ThreadPoolExecutor executor = new ThreadPoolExecutor(
    5, 10, 60L, TimeUnit.SECONDS,
    new LinkedBlockingQueue<>(100)
);

// 创建监控器
AdvancedThreadPoolMonitor monitor = ThreadPoolMonitorBuilder
    .createDefault()
    .addThreadPool("worker-pool", executor, MonitorableThreadPool.ThreadPoolType.FIXED)
    .addUtilizationStrategy(0.8, 0.95)  // 80%警告，95%严重
    .addQueueStrategy(80, 95)           // 队列80%警告，95%严重
    .buildAndStart();

// 获取监控状态
Optional<ThreadPoolStatus> status = monitor.getThreadPoolStatus("worker-pool");
status.ifPresent(s -> {
    System.out.printf("利用率: %.2f%%, 队列大小: %d%n", 
        s.getUtilization() * 100, s.getQueueSize());
});
```

### 2. 高级配置

```java
// 自定义监控配置
MonitorConfiguration config = MonitorConfiguration.builder()
    .monitorInterval(Duration.ofSeconds(3))
    .alertCheckInterval(Duration.ofSeconds(5))
    .maxHistoryRecords(1000)
    .adaptiveMonitoringEnabled(true)
    .predictiveAlertingEnabled(true)
    .build();

// 创建监控器
AdvancedThreadPoolMonitor monitor = ThreadPoolMonitorBuilder
    .custom(config)
    .addThreadPool("custom-pool", executor)
        .withType(MonitorableThreadPool.ThreadPoolType.CUSTOM)
        .withPriority(100)
        .withBusinessTag("service", "user-service")
        .withBusinessTag("environment", "production")
    .addCustomStrategy(new MyCustomStrategy())
    .buildAndStart();
```

### 3. 预设配置

```java
// Web应用监控
AdvancedThreadPoolMonitor webMonitor = ThreadPoolMonitorBuilder
    .forWebApplication()
    .addThreadPool("http-pool", httpExecutor)
    .addThreadPool("async-pool", asyncExecutor)
    .buildAndStart();

// 批处理应用监控
AdvancedThreadPoolMonitor batchMonitor = ThreadPoolMonitorBuilder
    .forBatchApplication()
    .addThreadPool("batch-pool", batchExecutor)
    .buildAndStart();

// 实时应用监控
AdvancedThreadPoolMonitor realtimeMonitor = ThreadPoolMonitorBuilder
    .forRealtimeApplication()
    .addThreadPool("realtime-pool", realtimeExecutor)
    .buildAndStart();
```

## 监控策略

### 内置策略

| 策略类型 | 描述 | 配置参数 |
|---------|------|----------|
| **利用率监控** | 监控线程池利用率 | `warningThreshold`, `criticalThreshold` |
| **队列监控** | 监控任务队列状态 | `queueWarningSize`, `queueCriticalSize` |
| **拒绝任务监控** | 监控任务拒绝情况 | `rejectionThreshold`, `timeWindow` |
| **健康检查** | 检查线程池健康状态 | `healthCheckInterval`, `unhealthyThreshold` |
| **性能分析** | 分析线程池性能指标 | `latencyThreshold`, `throughputThreshold` |

### 自定义策略

```java
public class CustomMonitorStrategy implements MonitorStrategy {
    @Override
    public String getName() {
        return "CustomStrategy";
    }
    
    @Override
    public boolean supports(MonitorableThreadPool threadPool) {
        return threadPool.getType() == MonitorableThreadPool.ThreadPoolType.CUSTOM;
    }
    
    @Override
    public MonitorResult monitor(MonitorableThreadPool threadPool, MonitorContext context) {
        // 实现自定义监控逻辑
        return new DefaultMonitorResult(...);
    }
}
```

## 异步监控

```java
// 异步状态获取
CompletableFuture<Optional<ThreadPoolStatus>> futureStatus = 
    monitor.getThreadPoolStatusAsync("pool-name");

// 异步监控检查
MonitorContext context = MonitorContext.createDefault();
CompletableFuture<List<MonitorStrategy.MonitorResult>> futureResults = 
    monitor.performMonitorCheckAsync(context);

// 批量状态获取
CompletableFuture<Map<String, ThreadPoolStatus>> allStatus = 
    monitor.getAllThreadPoolStatusAsync();

// 处理结果
futureResults.thenAccept(results -> {
    results.stream()
        .filter(MonitorStrategy.MonitorResult::needsAlert)
        .forEach(result -> {
            System.out.println("告警: " + result.getMessage());
            System.out.println("建议: " + result.getSuggestedAction());
        });
});
```

## 监控指标

### 线程池状态指标

- **基础指标**: 核心线程数、最大线程数、活跃线程数、当前线程池大小
- **任务指标**: 已提交任务总数、已完成任务总数、被拒绝任务总数
- **队列指标**: 队列当前任务数、队列剩余容量
- **利用率指标**: 线程池利用率、队列利用率
- **性能指标**: 平均响应时间、吞吐量、错误率

### 监控统计

```java
MonitorStatistics stats = monitor.getMonitorStatistics();
System.out.println("监控周期总数: " + stats.getTotalMonitorCycles());
System.out.println("平均监控延迟: " + stats.getAverageMonitoringLatency() + "ms");
System.out.println("成功率: " + stats.getSuccessRate() * 100 + "%");
System.out.println("告警总数: " + stats.getTotalAlerts());
```

## 配置参考

### 监控配置

```java
MonitorConfiguration config = MonitorConfiguration.builder()
    // 基础配置
    .monitorInterval(Duration.ofSeconds(5))        // 监控间隔
    .alertCheckInterval(Duration.ofSeconds(10))    // 告警检查间隔
    .metricsCollectionInterval(Duration.ofSeconds(3)) // 指标收集间隔
    
    // 历史数据配置
    .maxHistoryRecords(1000)                       // 最大历史记录数
    .historyRetentionPeriod(Duration.ofHours(12))  // 历史数据保留时间
    
    // 高级功能
    .adaptiveMonitoringEnabled(true)              // 自适应监控
    .predictiveAlertingEnabled(true)              // 预测性告警
    
    // 性能配置
    .monitorThreadPoolSize(4)                     // 监控线程池大小
    .batchSize(50)                                // 批处理大小
    .asyncProcessingTimeout(Duration.ofSeconds(30)) // 异步超时
    
    // 告警配置
    .alertSuppressionTime(Duration.ofMinutes(5))   // 告警抑制时间
    
    // 扩展配置
    .extendedConfig(Map.of(
        "customParam1", "value1",
        "customParam2", 42
    ))
    .build();
```

### 策略配置

```java
// 利用率策略配置
DefaultStrategyConfig utilizationConfig = DefaultStrategyConfig.forUtilization()
    .warningThreshold(0.8)
    .criticalThreshold(0.95)
    .checkInterval(Duration.ofSeconds(5))
    .build();

// 队列策略配置
DefaultStrategyConfig queueConfig = DefaultStrategyConfig.forQueue()
    .warningSize(80)
    .criticalSize(95)
    .utilizationWarningThreshold(0.8)
    .utilizationCriticalThreshold(0.95)
    .build();
```

## 最佳实践

### 1. 命名规范

```java
// 好的命名
monitor.addThreadPool("user-service-http-pool", executor);
monitor.addThreadPool("order-service-async-pool", executor);
monitor.addThreadPool("payment-service-batch-pool", executor);

// 避免的命名
monitor.addThreadPool("pool1", executor);
monitor.addThreadPool("thread-pool", executor);
```

### 2. 阈值设置

```java
// 根据业务特性设置合理阈值
// Web应用 - 响应时间敏感
.addUtilizationStrategy(0.7, 0.85)
.addQueueStrategy(50, 80)

// 批处理应用 - 吞吐量优先
.addUtilizationStrategy(0.9, 0.98)
.addQueueStrategy(200, 500)

// 实时应用 - 低延迟要求
.addUtilizationStrategy(0.6, 0.8)
.addQueueStrategy(20, 50)
```

### 3. 资源管理

```java
@Component
public class ThreadPoolMonitorManager {
    private AdvancedThreadPoolMonitor monitor;
    
    @PostConstruct
    public void init() {
        monitor = ThreadPoolMonitorBuilder
            .forWebApplication()
            .addThreadPool("main-pool", mainExecutor)
            .buildAndStart();
    }
    
    @PreDestroy
    public void cleanup() {
        if (monitor != null) {
            monitor.stop();
        }
    }
}
```

### 4. 告警处理

```java
// 实现告警处理器
public class AlertHandler {
    public void handleAlert(MonitorStrategy.MonitorResult result) {
        switch (result.getAlertLevel()) {
            case CRITICAL:
                // 发送紧急通知
                sendUrgentNotification(result);
                break;
            case ERROR:
                // 发送错误通知
                sendErrorNotification(result);
                break;
            case WARN:
                // 记录警告日志
                logWarning(result);
                break;
            case INFO:
                // 记录信息日志
                logInfo(result);
                break;
        }
    }
}
```

## 性能优化

### 1. 监控频率调优

```java
// 高频场景 - 实时应用
MonitorConfiguration highFreqConfig = MonitorConfiguration.builder()
    .monitorInterval(Duration.ofSeconds(1))
    .alertCheckInterval(Duration.ofSeconds(2))
    .build();

// 低频场景 - 批处理应用
MonitorConfiguration lowFreqConfig = MonitorConfiguration.builder()
    .monitorInterval(Duration.ofSeconds(30))
    .alertCheckInterval(Duration.ofMinutes(1))
    .build();
```

### 2. 内存优化

```java
// 控制历史数据大小
MonitorConfiguration memoryOptimizedConfig = MonitorConfiguration.builder()
    .maxHistoryRecords(500)                        // 减少历史记录
    .historyRetentionPeriod(Duration.ofHours(6))   // 缩短保留时间
    .batchSize(20)                                 // 减少批处理大小
    .build();
```

### 3. 异步优化

```java
// 异步处理配置
MonitorConfiguration asyncConfig = MonitorConfiguration.builder()
    .monitorThreadPoolSize(8)                      // 增加监控线程
    .asyncProcessingTimeout(Duration.ofSeconds(10)) // 设置合理超时
    .build();
```

## 故障排查

### 常见问题

1. **监控器无法启动**
   - 检查配置有效性: `config.isValid()`
   - 验证线程池状态
   - 查看日志错误信息

2. **告警过于频繁**
   - 调整告警阈值
   - 增加告警抑制时间
   - 检查监控策略配置

3. **监控延迟过高**
   - 减少监控频率
   - 增加监控线程池大小
   - 优化监控策略逻辑

4. **内存使用过高**
   - 减少历史记录数量
   - 缩短数据保留时间
   - 检查是否有内存泄漏

### 调试技巧

```java
// 启用详细日志
MonitorConfiguration debugConfig = MonitorConfiguration.builder()
    .extendedConfig(Map.of(
        "logLevel", "DEBUG",
        "enableDetailedMetrics", true
    ))
    .build();

// 获取详细统计信息
MonitorStatistics stats = monitor.getMonitorStatistics();
System.out.println("详细统计: " + stats.getDetailedStats());

// 检查监控器状态
System.out.println("监控状态: " + monitor.getMonitoringState());
System.out.println("注册的线程池: " + monitor.getRegisteredThreadPools());
```

## 项目结构

```
konors-threadPool-monitor/
├── src/
│   ├── main/
│   │   └── java/
│   │       └── com/konors/threadpool/monitor/
│   │           ├── core/                    # 核心接口和类
│   │           │   ├── MonitorableThreadPool.java
│   │           │   ├── ThreadPoolStatus.java
│   │           │   ├── ThreadPoolMetrics.java
│   │           │   └── ThreadPoolConfiguration.java
│   │           ├── strategy/                # 监控策略
│   │           │   ├── MonitorStrategy.java
│   │           │   ├── UtilizationMonitorStrategy.java
│   │           │   └── QueueMonitorStrategy.java
│   │           ├── factory/                 # 工厂类
│   │           │   ├── MonitorStrategyFactory.java
│   │           │   └── DefaultMonitorStrategyFactory.java
│   │           ├── config/                  # 配置类
│   │           │   ├── MonitorConfiguration.java
│   │           │   ├── MonitorContext.java
│   │           │   └── DefaultStrategyConfig.java
│   │           ├── monitor/                 # 监控器实现
│   │           │   ├── AdvancedThreadPoolMonitor.java
│   │           │   └── DefaultAdvancedThreadPoolMonitor.java
│   │           ├── builder/                 # 建造者
│   │           │   └── ThreadPoolMonitorBuilder.java
│   │           └── impl/                    # 默认实现
│   │               └── DefaultMonitorableThreadPool.java
│   └── test/
│       └── java/
│           └── com/konors/threadpool/monitor/
│               └── integration/
│                   └── IntegrationTest.java # 集成测试
├── docs/
│   ├── ADVANCED_MONITOR_USAGE.md           # 使用指南
│   └── API_REFERENCE.md                    # API参考
├── README.md                               # 项目说明
└── pom.xml                                 # Maven配置
```

## 版本历史

### v2.0.0 (当前版本)
- ✨ 全新的架构设计，采用策略模式和工厂模式
- 🚀 支持异步监控和批量操作
- 🎯 提供多种内置监控策略
- 🔧 支持自定义策略和配置
- 📊 增强的监控指标和统计信息
- 🏗️ 流式API和建造者模式
- 📈 预测性告警和自适应监控

### v1.0.0
- 基础的线程池监控功能
- 简单的状态收集和指标计算

## 贡献指南

1. Fork 项目
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 打开 Pull Request

## 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情。

## 联系方式

- 作者: Konors
- 邮箱: konors@example.com
- 项目链接: [https://github.com/konors/threadpool-monitor](https://github.com/konors/threadpool-monitor)

## 致谢

感谢所有为这个项目做出贡献的开发者和用户。

---

**注意**: 这是一个重构后的高级版本，相比原始版本在架构设计、扩展性、性能和易用性方面都有显著提升。建议在生产环境中使用前进行充分的测试。