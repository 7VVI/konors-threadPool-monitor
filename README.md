# Konors ThreadPool Monitor - 高级线程池监控系统

[![Java](https://img.shields.io/badge/Java-11+-orange.svg)](https://www.oracle.com/java/)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)
[![Build Status](https://img.shields.io/badge/Build-Passing-green.svg)](#)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-2.5+-brightgreen.svg)](https://spring.io/projects/spring-boot)

## 项目概述

Konors ThreadPool Monitor 是一个企业级的Java线程池监控系统，采用现代化的设计模式和架构，提供全面的线程池监控、告警和分析能力。支持传统Java应用和Spring Boot应用，提供简化的API和自动配置功能。

### 核心特性

- 🎯 **策略化监控**: 支持多种监控策略，可插拔式架构
- 🔧 **高度可扩展**: 基于接口设计，支持自定义策略和配置
- ⚡ **异步处理**: 非阻塞监控，支持高并发场景
- 📊 **丰富指标**: 提供利用率、队列状态、性能等多维度指标
- 🚨 **智能告警**: 多级别告警机制，支持自定义阈值
- 🏗️ **建造者模式**: 流式API，简化配置和使用
- 📈 **实时监控**: 支持实时状态查询和历史数据分析
- 🌱 **Spring Boot 支持**: 提供自动配置和REST API
- 🎛️ **简化注册**: 支持最简单的线程池名称+执行器注册方式

## 架构设计

### 核心组件

```
┌─────────────────────────────────────────────────────────────┐
│                    应用层 (Application Layer)                │
├─────────────────────────────────────────────────────────────┤
│  Spring Boot Starter │ REST API │ ThreadPoolUtil             │
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

### 1. Spring Boot 集成（推荐）

#### 添加依赖

```xml
<dependency>
    <groupId>com.konors</groupId>
    <artifactId>konors-threadpool-monitor-spring-boot-starter</artifactId>
    <version>1.0.0</version>
</dependency>
```

#### 配置属性

```properties
# 启用监控
konors.threadpool.monitor.enabled=true

# 监控间隔(毫秒)
konors.threadpool.monitor.monitor-interval=5000

# 是否异步监控
konors.threadpool.monitor.async-monitoring=true

# 监控线程池大小
konors.threadpool.monitor.monitor-thread-pool-size=2

# 告警配置
konors.threadpool.monitor.alert-enabled=true
konors.threadpool.monitor.alert-suppression-time=300000

# 预测性告警
konors.threadpool.monitor.predictive-alert-enabled=false

# 健康检查
konors.threadpool.monitor.health-check-enabled=true

# 利用率阈值
konors.threadpool.monitor.default-utilization-warning-threshold=0.75
konors.threadpool.monitor.default-utilization-critical-threshold=0.90

# 队列阈值
konors.threadpool.monitor.default-queue-warning-threshold=100
konors.threadpool.monitor.default-queue-critical-threshold=300

# JMX和指标
konors.threadpool.monitor.jmx-enabled=false
konors.threadpool.monitor.metrics-enabled=false

# 数据保留时间(毫秒)
konors.threadpool.monitor.data-retention-time=7200000
```

#### 使用监控器

```java
@Service
public class TaskService {
    
    @Autowired
    private AdvancedThreadPoolMonitor threadPoolMonitor;
    
    @PostConstruct
    public void init() {
        // 创建线程池
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
            5, 10, 60L, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(100)
        );
        
        // 简化注册 - 只需名称和执行器
        threadPoolMonitor.registerThreadPool("task-pool", executor);
        
        // 也可以指定优先级
        threadPoolMonitor.registerThreadPool("priority-pool", executor, 200);
    }
}
```

### 2. REST API 接口

Spring Boot 集成自动提供以下REST接口：

| 接口 | 方法 | 描述 |
|------|------|------|
| `/api/threadpool/monitor/status` | GET | 获取所有线程池状态 |
| `/api/threadpool/monitor/status/{poolName}` | GET | 获取指定线程池状态 |
| `/api/threadpool/monitor/statistics` | GET | 获取监控统计信息 |
| `/api/threadpool/monitor/pools` | GET | 获取已注册线程池名称列表 |
| `/api/threadpool/monitor/start` | POST | 启动监控 |
| `/api/threadpool/monitor/stop` | POST | 停止监控 |
| `/api/threadpool/monitor/pause` | POST | 暂停监控 |
| `/api/threadpool/monitor/resume` | POST | 恢复监控 |
| `/api/threadpool/monitor/state` | GET | 获取监控状态 |

### 3. 传统Java应用使用

#### 基本使用

```java
// 创建线程池
ThreadPoolExecutor executor = new ThreadPoolExecutor(
    5, 10, 60L, TimeUnit.SECONDS,
    new LinkedBlockingQueue<>(100)
);

// 方式1: 使用构建器（推荐）
AdvancedThreadPoolMonitor monitor = ThreadPoolMonitorBuilder
    .createDefault()
    .addThreadPool("worker-pool", executor) // 简化注册
    .addUtilizationStrategy(0.8, 0.95)      // 80%警告，95%严重
    .addQueueStrategy(80, 95)               // 队列80%警告，95%严重
    .buildAndStart();

// 方式2: 直接使用监控器API
AdvancedThreadPoolMonitor monitor = new DefaultAdvancedThreadPoolMonitor();
monitor.registerThreadPool("worker-pool", executor);  // 最简单注册
monitor.startMonitoring();

// 获取监控状态
Optional<ThreadPoolStatus> status = monitor.getThreadPoolStatus("worker-pool");
status.ifPresent(s -> {
    System.out.printf("利用率: %.2f%%, 队列大小: %d%n", 
        s.getUtilization() * 100, s.getQueueSize());
});
```

#### 高级配置

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
    .create()
    .withMonitorInterval(Duration.ofSeconds(3))
    .withAdaptiveMonitoring(true)
    .addThreadPool("custom-pool", executor)
    .addCustomStrategy(new MyCustomStrategy())
    .buildAndStart();
```

#### 预设配置

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

## 简化线程池注册

### 背景

传统方式需要创建复杂的`MonitorableThreadPool`对象，现在支持最简化的注册方式：

### 新的简化API

```java
// 最简单的注册方式 - 只需要名称和执行器
monitor.registerThreadPool("pool-name", executor);

// 指定优先级
monitor.registerThreadPool("pool-name", executor, 200);

// 在构建器中使用
ThreadPoolMonitorBuilder
    .createDefault()
    .addThreadPool("pool1", executor1)
    .addThreadPool("pool2", executor2, 150)  // 带优先级
    .buildAndStart();
```

### 自动推断特性

系统会自动：
- 推断线程池类型（FIXED、CACHED、SINGLE、CUSTOM）
- 创建`ThreadPoolConfiguration`配置
- 设置默认优先级（100）
- 设置健康状态为true

### 支持的线程池类型

| 线程池类型 | 自动识别条件 |
|-----------|-------------|
| `SINGLE` | 核心线程数 = 最大线程数 = 1 |
| `FIXED` | 核心线程数 = 最大线程数 > 1 |
| `CACHED` | 核心线程数 = 0, 最大线程数 = Integer.MAX_VALUE |
| `CUSTOM` | 其他所有情况 |

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

### Spring Boot 配置属性

```properties
# 基础配置
konors.threadpool.monitor.enabled=true
konors.threadpool.monitor.monitor-interval=5000
konors.threadpool.monitor.async-monitoring=true

# 监控配置
konors.threadpool.monitor.monitor-thread-pool-size=2
konors.threadpool.monitor.alert-enabled=true
konors.threadpool.monitor.alert-suppression-time=300000

# 高级功能
konors.threadpool.monitor.predictive-alert-enabled=false
konors.threadpool.monitor.performance-analysis-enabled=true
konors.threadpool.monitor.health-check-enabled=true

# 阈值配置
konors.threadpool.monitor.default-utilization-warning-threshold=0.75
konors.threadpool.monitor.default-utilization-critical-threshold=0.90
konors.threadpool.monitor.default-queue-warning-threshold=100
konors.threadpool.monitor.default-queue-critical-threshold=300

# 集成配置
konors.threadpool.monitor.jmx-enabled=false
konors.threadpool.monitor.metrics-enabled=false
konors.threadpool.monitor.data-retention-time=7200000
```

### 监控配置（编程方式）

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
    .alertSuppressionPeriod(Duration.ofMinutes(5))   // 告警抑制时间
    
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
monitor.registerThreadPool("user-service-http-pool", executor);
monitor.registerThreadPool("order-service-async-pool", executor);
monitor.registerThreadPool("payment-service-batch-pool", executor);

// 避免的命名
monitor.registerThreadPool("pool1", executor);
monitor.registerThreadPool("thread-pool", executor);
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

### 3. Spring Boot 集成最佳实践

```java
@Component
public class ThreadPoolManager {
    
    @Autowired
    private AdvancedThreadPoolMonitor monitor;
    
    private ThreadPoolExecutor taskExecutor;
    private ThreadPoolExecutor asyncExecutor;
    
    @PostConstruct
    public void init() {
        // 创建线程池
        taskExecutor = new ThreadPoolExecutor(5, 10, 60L, TimeUnit.SECONDS, 
            new LinkedBlockingQueue<>(100));
        asyncExecutor = new ThreadPoolExecutor(3, 6, 30L, TimeUnit.SECONDS, 
            new LinkedBlockingQueue<>(50));
        
        // 注册监控
        monitor.registerThreadPool("task-executor", taskExecutor);
        monitor.registerThreadPool("async-executor", asyncExecutor, 200);
    }
    
    @PreDestroy
    public void cleanup() {
        if (taskExecutor != null) taskExecutor.shutdown();
        if (asyncExecutor != null) asyncExecutor.shutdown();
    }
}
```

### 4. 告警处理

```java
// 实现告警处理器
@Component
public class AlertHandler {
    
    @EventListener
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

3. **Spring Boot 自动配置失效**
   - 检查是否正确引入starter依赖
   - 确认配置属性前缀正确：`konors.threadpool.monitor`
   - 检查`@EnableAutoConfiguration`是否生效

4. **REST API 无法访问**
   - 确认Spring Boot Web依赖已添加
   - 检查URL路径：`/api/threadpool/monitor/*`
   - 验证监控器是否已启动

5. **线程池注册失败**
   - 检查线程池名称是否重复
   - 验证ThreadPoolExecutor是否为null
   - 查看注册结果的错误信息

### 调试模式

```properties
# 启用调试日志
logging.level.com.konors.threadpool.monitor=DEBUG

# 或在Java代码中
@PostConstruct
public void enableDebug() {
    Logger logger = LoggerFactory.getLogger("com.konors.threadpool.monitor");
    if (logger instanceof ch.qos.logback.classic.Logger) {
        ((ch.qos.logback.classic.Logger) logger).setLevel(Level.DEBUG);
    }
}
```

## 版本更新

### v1.0.0 新特性

- ✅ **简化线程池注册**: 支持仅使用名称和执行器注册
- ✅ **Spring Boot Starter**: 提供自动配置和REST API
- ✅ **REST监控接口**: 标准化的HTTP API
- ✅ **自动类型推断**: 智能识别线程池类型
- ✅ **配置属性支持**: 通过application.properties配置
- ✅ **ThreadPoolUtil工具类**: 简化线程池包装

### 迁移指南

从复杂注册方式迁移到简化方式：

```java
// 原来的方式
MonitorableThreadPool pool = DefaultMonitorableThreadPool.builder()
    .name("worker-pool")
    .executor(executor)
    .type(MonitorableThreadPool.ThreadPoolType.FIXED)
    .configuration(config)
    .priority(100)
    .build();
monitor.registerThreadPool(pool);

// 新的简化方式
monitor.registerThreadPool("worker-pool", executor);
```

## 项目结构

```
konors-threadPool-monitor/
├── src/main/java/
│   └── com/konors/threadpool/monitor/
│       ├── core/                    # 核心监控逻辑
│       │   ├── abstraction/         # 接口和抽象类
│       │   ├── impl/                # 具体实现
│       │   ├── builder/             # 构建器
│       │   ├── factory/             # 工厂类
│       │   ├── strategy/            # 监控策略
│       │   └── util/                # 工具类
│       ├── controller/              # REST控制器
│       ├── starter/                 # Spring Boot Starter
│       ├── common/                  # 通用组件
│       └── config/                  # 配置类
├── src/main/resources/
│   ├── META-INF/spring/             # 自动配置
│   └── application.properties       # 默认配置
└── src/test/                        # 测试代码
```

## 贡献指南

1. Fork 项目
2. 创建功能分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 打开 Pull Request

## 许可证

本项目使用 MIT 许可证。详情请见 [LICENSE](LICENSE) 文件。

## 联系方式

- 作者: zhangYh
- 项目链接: [https://github.com/konors/threadpool-monitor](https://github.com/konors/threadpool-monitor)
- 问题反馈: [Issues](https://github.com/konors/threadpool-monitor/issues)

---

**如果这个项目对你有帮助，请给个 ⭐ Star！**