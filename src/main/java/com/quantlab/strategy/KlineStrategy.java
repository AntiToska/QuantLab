package com.quantlab.strategy;

import com.quantlab.marketdata.model.KlineEvent;

/**
 * 基于 K 线驱动的最小策略接口。
 * <p>
 * Backtest Core 当前先只依赖这个接口跑通事件循环。
 */
public interface KlineStrategy {

    String name();

    StrategySignal onKline(KlineEvent event);
}
