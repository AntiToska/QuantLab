package com.quantlab.strategy;

/**
 * 策略信号。
 * <p>
 * 当前先保留最小集合，后续接入模拟撮合时再扩展数量、价格等细节。
 */
public enum StrategySignal {
    BUY,
    SELL,
    HOLD
}
