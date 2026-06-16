package com.quantlab.marketdata.connector.binance;

import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import org.springframework.stereotype.Component;

/**
 * 默认 Binance 调度器实现。
 */
@Component
public class DefaultBinanceScheduler implements BinanceScheduler {

    private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {
        @Override
        public Thread newThread(Runnable runnable) {
            Thread thread = new Thread(runnable, "binance-ws-scheduler");
            thread.setDaemon(true);
            return thread;
        }
    });

    @Override
    public ScheduledFuture<?> schedule(Runnable task, Duration delay) {
        return executorService.schedule(task, delay.toMillis(), TimeUnit.MILLISECONDS);
    }

    @Override
    public ScheduledFuture<?> scheduleAtFixedRate(Runnable task, Duration initialDelay, Duration period) {
        return executorService.scheduleAtFixedRate(
                task,
                initialDelay.toMillis(),
                period.toMillis(),
                TimeUnit.MILLISECONDS
        );
    }
}
