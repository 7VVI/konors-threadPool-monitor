//package com.konors.threadpool.monitor.integration;
//
//import com.konors.threadpool.monitor.core.*;
//import com.konors.threadpool.monitor.strategy.*;
//import com.konors.threadpool.monitor.factory.*;
//import com.konors.threadpool.monitor.config.*;
//import com.konors.threadpool.monitor.builder.*;
//import org.junit.jupiter.api.*;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.junit.jupiter.MockitoExtension;
//
//import java.time.Duration;
//import java.util.*;
//import java.util.concurrent.*;
//import java.util.concurrent.atomic.AtomicInteger;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.awaitility.Awaitility.*;
//
///**
// * 集成测试类，演示高级线程池监控系统的完整使用流程
// *
// * @author konors
// * @version 1.0
// */
//@ExtendWith(MockitoExtension.class)
//@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
//public class IntegrationTest {
//
//    private AdvancedThreadPoolMonitor monitor;
//    private ThreadPoolExecutor testExecutor;
//    private MonitorableThreadPool monitorablePool;
//
//    @BeforeEach
//    void setUp() {
//        // 创建测试用的线程池
//        testExecutor = new ThreadPoolExecutor(
//            2, 5, 60L, TimeUnit.SECONDS,
//            new LinkedBlockingQueue<>(10),
//            new ThreadFactory() {
//                private final AtomicInteger counter = new AtomicInteger(0);
//                @Override
//                public Thread newThread(Runnable r) {
//                    Thread t = new Thread(r, "test-worker-" + counter.incrementAndGet());
//                    t.setDaemon(true);
//                    return t;
//                }
//            },
//            new ThreadPoolExecutor.CallerRunsPolicy()
//        );
//
//        // 创建可监控的线程池
//        monitorablePool = DefaultMonitorableThreadPool.builder()
//            .name("test-pool")
//            .executor(testExecutor)
//            .type(MonitorableThreadPool.ThreadPoolType.CUSTOM)
//            .priority(100)
//            .businessTag("service", "test-service")
//            .businessTag("environment", "test")
//            .build();
//    }
//
//    @AfterEach
//    void tearDown() {
//        if (monitor != null) {
//            monitor.stop();
//        }
//        if (testExecutor != null && !testExecutor.isShutdown()) {
//            testExecutor.shutdown();
//            try {
//                if (!testExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
//                    testExecutor.shutdownNow();
//                }
//            } catch (InterruptedException e) {
//                testExecutor.shutdownNow();
//                Thread.currentThread().interrupt();
//            }
//        }
//    }
//
//    @Test
//    @Order(1)
//    @DisplayName("基本监控功能测试")
//    void testBasicMonitoring() {
//        // 使用构建器创建监控器
//        monitor = ThreadPoolMonitorBuilder
//            .createDefault()
//            .monitorInterval(Duration.ofMillis(100))
//            .alertCheckInterval(Duration.ofMillis(200))
//            .addThreadPool(monitorablePool)
//            .addUtilizationStrategy(0.6, 0.8)
//            .addQueueStrategy(5, 8)
//            .build();
//
//        // 启动监控
//        monitor.start();
//
//        // 验证监控器状态
//        assertEquals(AdvancedThreadPoolMonitor.MonitoringState.RUNNING, monitor.getMonitoringState());
//
//        // 验证线程池注册
//        assertTrue(monitor.getRegisteredThreadPools().contains("test-pool"));
//
//        // 获取线程池状态
//        Optional<ThreadPoolStatus> status = monitor.getThreadPoolStatus("test-pool");
//        assertTrue(status.isPresent());
//        assertEquals("test-pool", status.get().getPoolName());
//
//        // 验证监控统计
//        AdvancedThreadPoolMonitor.MonitorStatistics stats = monitor.getMonitorStatistics();
//        assertNotNull(stats);
//        assertTrue(stats.getTotalMonitorCycles() >= 0);
//    }
//
//    @Test
//    @Order(2)
//    @DisplayName("利用率监控策略测试")
//    void testUtilizationMonitoring() throws InterruptedException {
//        // 创建带有利用率监控的监控器
//        monitor = ThreadPoolMonitorBuilder
//            .createDefault()
//            .monitorInterval(Duration.ofMillis(50))
//            .addThreadPool(monitorablePool)
//            .addUtilizationStrategy(0.3, 0.6) // 设置较低的阈值便于测试
//            .build();
//
//        monitor.start();
//
//        // 提交任务以增加利用率
//        CountDownLatch latch = new CountDownLatch(3);
//        for (int i = 0; i < 3; i++) {
//            testExecutor.submit(() -> {
//                try {
//                    Thread.sleep(500); // 模拟长时间任务
//                } catch (InterruptedException e) {
//                    Thread.currentThread().interrupt();
//                } finally {
//                    latch.countDown();
//                }
//            });
//        }
//
//        // 等待一段时间让监控器检测到高利用率
//        Thread.sleep(200);
//
//        // 执行监控检查
//        MonitorContext context = MonitorContext.createDefault();
//        List<MonitorStrategy.MonitorResult> results = monitor.performMonitorCheck(context);
//
//        // 验证是否有利用率告警
//        boolean hasUtilizationAlert = results.stream()
//            .anyMatch(result -> result.needsAlert() &&
//                     result.getMessage().toLowerCase().contains("utilization"));
//
//        assertTrue(hasUtilizationAlert, "应该检测到利用率告警");
//
//        // 等待任务完成
//        latch.await(2, TimeUnit.SECONDS);
//    }
//
//    @Test
//    @Order(3)
//    @DisplayName("队列监控策略测试")
//    void testQueueMonitoring() throws InterruptedException {
//        // 创建带有队列监控的监控器
//        monitor = ThreadPoolMonitorBuilder
//            .createDefault()
//            .monitorInterval(Duration.ofMillis(50))
//            .addThreadPool(monitorablePool)
//            .addQueueStrategy(3, 6) // 设置较低的阈值便于测试
//            .build();
//
//        monitor.start();
//
//        // 先用长任务占满所有线程
//        CountDownLatch blockingLatch = new CountDownLatch(5);
//        for (int i = 0; i < 5; i++) {
//            testExecutor.submit(() -> {
//                try {
//                    Thread.sleep(1000);
//                } catch (InterruptedException e) {
//                    Thread.currentThread().interrupt();
//                } finally {
//                    blockingLatch.countDown();
//                }
//            });
//        }
//
//        // 再提交更多任务到队列
//        for (int i = 0; i < 8; i++) {
//            testExecutor.submit(() -> {
//                try {
//                    Thread.sleep(100);
//                } catch (InterruptedException e) {
//                    Thread.currentThread().interrupt();
//                }
//            });
//        }
//
//        // 等待队列积压
//        Thread.sleep(100);
//
//        // 执行监控检查
//        MonitorContext context = MonitorContext.createDefault();
//        List<MonitorStrategy.MonitorResult> results = monitor.performMonitorCheck(context);
//
//        // 验证是否有队列告警
//        boolean hasQueueAlert = results.stream()
//            .anyMatch(result -> result.needsAlert() &&
//                     result.getMessage().toLowerCase().contains("queue"));
//
//        assertTrue(hasQueueAlert, "应该检测到队列告警");
//
//        // 等待任务完成
//        blockingLatch.await(3, TimeUnit.SECONDS);
//    }
//
//    @Test
//    @Order(4)
//    @DisplayName("异步监控测试")
//    void testAsyncMonitoring() {
//        monitor = ThreadPoolMonitorBuilder
//            .createDefault()
//            .addThreadPool(monitorablePool)
//            .addUtilizationStrategy(0.8, 0.9)
//            .build();
//
//        monitor.start();
//
//        // 测试异步状态获取
//        CompletableFuture<Optional<ThreadPoolStatus>> futureStatus =
//            monitor.getThreadPoolStatusAsync("test-pool");
//
//        assertDoesNotThrow(() -> {
//            Optional<ThreadPoolStatus> status = futureStatus.get(1, TimeUnit.SECONDS);
//            assertTrue(status.isPresent());
//            assertEquals("test-pool", status.get().getPoolName());
//        });
//
//        // 测试异步监控检查
//        MonitorContext context = MonitorContext.createDefault();
//        CompletableFuture<List<MonitorStrategy.MonitorResult>> futureResults =
//            monitor.performMonitorCheckAsync(context);
//
//        assertDoesNotThrow(() -> {
//            List<MonitorStrategy.MonitorResult> results = futureResults.get(1, TimeUnit.SECONDS);
//            assertNotNull(results);
//        });
//    }
//
//    @Test
//    @Order(5)
//    @DisplayName("自定义监控策略测试")
//    void testCustomMonitorStrategy() {
//        // 创建自定义监控策略
//        MonitorStrategy customStrategy = new MonitorStrategy() {
//            @Override
//            public String getName() {
//                return "CustomTestStrategy";
//            }
//
//            @Override
//            public int getPriority() {
//                return 50;
//            }
//
//            @Override
//            public boolean supports(MonitorableThreadPool threadPool) {
//                return threadPool.getBusinessTags().containsKey("service");
//            }
//
//            @Override
//            public MonitorResult monitor(MonitorableThreadPool threadPool, MonitorContext context) {
//                String serviceName = threadPool.getBusinessTags().get("service");
//                boolean needsAlert = "test-service".equals(serviceName);
//
//                return new MonitorResult() {
//                    @Override
//                    public boolean needsAlert() {
//                        return needsAlert;
//                    }
//
//                    @Override
//                    public AlertLevel getAlertLevel() {
//                        return AlertLevel.INFO;
//                    }
//
//                    @Override
//                    public String getMessage() {
//                        return "Custom strategy detected service: " + serviceName;
//                    }
//
//                    @Override
//                    public String getSuggestedAction() {
//                        return "Review service configuration";
//                    }
//
//                    @Override
//                    public Map<String, Object> getExtendedData() {
//                        return Map.of("serviceName", serviceName);
//                    }
//                };
//            }
//        };
//
//        // 创建监控器并添加自定义策略
//        monitor = ThreadPoolMonitorBuilder
//            .createDefault()
//            .addThreadPool(monitorablePool)
//            .build();
//
//        monitor.addMonitorStrategy(customStrategy);
//        monitor.start();
//
//        // 执行监控检查
//        MonitorContext context = MonitorContext.createDefault();
//        List<MonitorStrategy.MonitorResult> results = monitor.performMonitorCheck(context);
//
//        // 验证自定义策略是否生效
//        boolean hasCustomAlert = results.stream()
//            .anyMatch(result -> result.getMessage().contains("Custom strategy"));
//
//        assertTrue(hasCustomAlert, "自定义策略应该生效");
//    }
//
//    @Test
//    @Order(6)
//    @DisplayName("监控器生命周期测试")
//    void testMonitorLifecycle() {
//        monitor = ThreadPoolMonitorBuilder
//            .createDefault()
//            .addThreadPool(monitorablePool)
//            .build();
//
//        // 测试启动
//        assertEquals(AdvancedThreadPoolMonitor.MonitoringState.STOPPED, monitor.getMonitoringState());
//        monitor.start();
//        assertEquals(AdvancedThreadPoolMonitor.MonitoringState.RUNNING, monitor.getMonitoringState());
//
//        // 测试暂停
//        monitor.pause();
//        assertEquals(AdvancedThreadPoolMonitor.MonitoringState.PAUSED, monitor.getMonitoringState());
//
//        // 测试恢复
//        monitor.resume();
//        assertEquals(AdvancedThreadPoolMonitor.MonitoringState.RUNNING, monitor.getMonitoringState());
//
//        // 测试停止
//        monitor.stop();
//        assertEquals(AdvancedThreadPoolMonitor.MonitoringState.STOPPED, monitor.getMonitoringState());
//    }
//
//    @Test
//    @Order(7)
//    @DisplayName("批量操作测试")
//    void testBatchOperations() {
//        // 创建多个线程池
//        ThreadPoolExecutor executor2 = new ThreadPoolExecutor(
//            1, 3, 30L, TimeUnit.SECONDS,
//            new ArrayBlockingQueue<>(5)
//        );
//
//        MonitorableThreadPool pool2 = DefaultMonitorableThreadPool.builder()
//            .name("test-pool-2")
//            .executor(executor2)
//            .type(MonitorableThreadPool.ThreadPoolType.FIXED)
//            .build();
//
//        try {
//            monitor = ThreadPoolMonitorBuilder
//                .createDefault()
//                .addThreadPool(monitorablePool)
//                .addThreadPool(pool2)
//                .build();
//
//            monitor.start();
//
//            // 测试批量状态获取
//            CompletableFuture<Map<String, ThreadPoolStatus>> allStatus =
//                monitor.getAllThreadPoolStatusAsync();
//
//            assertDoesNotThrow(() -> {
//                Map<String, ThreadPoolStatus> statusMap = allStatus.get(1, TimeUnit.SECONDS);
//                assertEquals(2, statusMap.size());
//                assertTrue(statusMap.containsKey("test-pool"));
//                assertTrue(statusMap.containsKey("test-pool-2"));
//            });
//
//            // 测试健康线程池列表
//            List<String> healthyPools = monitor.getHealthyThreadPools();
//            assertEquals(2, healthyPools.size());
//
//        } finally {
//            executor2.shutdown();
//        }
//    }
//
//    @Test
//    @Order(8)
//    @DisplayName("配置验证测试")
//    void testConfigurationValidation() {
//        // 测试有效配置
//        MonitorConfiguration validConfig = MonitorConfiguration.builder()
//            .monitorInterval(Duration.ofSeconds(1))
//            .alertCheckInterval(Duration.ofSeconds(2))
//            .maxHistoryRecords(100)
//            .build();
//
//        assertTrue(validConfig.isValid());
//
//        // 测试无效配置
//        assertThrows(IllegalArgumentException.class, () -> {
//            MonitorConfiguration.builder()
//                .monitorInterval(Duration.ofMillis(-1)) // 无效的负值
//                .build();
//        });
//    }
//
//    @Test
//    @Order(9)
//    @DisplayName("预设配置测试")
//    void testPresetConfigurations() {
//        // 测试Web应用配置
//        AdvancedThreadPoolMonitor webMonitor = ThreadPoolMonitorBuilder
//            .forWebApplication()
//            .addThreadPool(monitorablePool)
//            .build();
//
//        assertNotNull(webMonitor);
//
//        // 测试批处理应用配置
//        AdvancedThreadPoolMonitor batchMonitor = ThreadPoolMonitorBuilder
//            .forBatchApplication()
//            .addThreadPool(monitorablePool)
//            .build();
//
//        assertNotNull(batchMonitor);
//
//        // 测试实时应用配置
//        AdvancedThreadPoolMonitor realtimeMonitor = ThreadPoolMonitorBuilder
//            .forRealtimeApplication()
//            .addThreadPool(monitorablePool)
//            .build();
//
//        assertNotNull(realtimeMonitor);
//
//        // 清理资源
//        webMonitor.stop();
//        batchMonitor.stop();
//        realtimeMonitor.stop();
//    }
//
//    @Test
//    @Order(10)
//    @DisplayName("性能基准测试")
//    void testPerformanceBenchmark() {
//        monitor = ThreadPoolMonitorBuilder
//            .createDefault()
//            .monitorInterval(Duration.ofMillis(10)) // 高频监控
//            .addThreadPool(monitorablePool)
//            .addUtilizationStrategy(0.8, 0.9)
//            .addQueueStrategy(80, 95)
//            .build();
//
//        monitor.start();
//
//        // 提交一些任务
//        for (int i = 0; i < 50; i++) {
//            testExecutor.submit(() -> {
//                try {
//                    Thread.sleep(10);
//                } catch (InterruptedException e) {
//                    Thread.currentThread().interrupt();
//                }
//            });
//        }
//
//        // 运行一段时间收集性能数据
//        await().atMost(Duration.ofSeconds(2))
//               .until(() -> {
//                   AdvancedThreadPoolMonitor.MonitorStatistics stats = monitor.getMonitorStatistics();
//                   return stats.getTotalMonitorCycles() > 10;
//               });
//
//        // 验证性能指标
//        AdvancedThreadPoolMonitor.MonitorStatistics stats = monitor.getMonitorStatistics();
//        assertTrue(stats.getTotalMonitorCycles() > 0);
//        assertTrue(stats.getAverageMonitoringLatency() >= 0);
//
//        System.out.println("Performance Stats:");
//        System.out.println("Total cycles: " + stats.getTotalMonitorCycles());
//        System.out.println("Average latency: " + stats.getAverageMonitoringLatency() + "ms");
//        System.out.println("Success rate: " + stats.getSuccessRate() * 100 + "%");
//    }
//}