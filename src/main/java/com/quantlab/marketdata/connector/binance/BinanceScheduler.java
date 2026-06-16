package com.quantlab.marketdata.connector.binance;

import java.time.Duration;
import java.util.concurrent.ScheduledFuture;

/**
 * Binance 传输层调度器抽象。
 * <p>
 * 用于管理重连和心跳定时任务，便于测试替换真实调度实现。
 */
public interface BinanceScheduler {

    ScheduledFuture<?> schedule(Runnable task, Duration delay);

    ScheduledFuture<?> scheduleAtFixedRate(Runnable task, Duration initialDelay, Duration period);
}
