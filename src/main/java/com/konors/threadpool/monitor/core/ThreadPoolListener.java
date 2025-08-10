package com.konors.threadpool.monitor.core;

/**
 * @author zhangYh
 * @Date 2025/8/10 10:30
 * @desc
 */
public interface ThreadPoolListener {

   void onStatusUpdate(String poolName, ThreadPoolStatus status);
}
