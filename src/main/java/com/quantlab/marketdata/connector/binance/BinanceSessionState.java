package com.quantlab.marketdata.connector.binance;

/**
 * Binance 会话状态。
 */
public enum BinanceSessionState {
    CONNECTING,
    OPEN,
    RECONNECTING,
    CLOSED
}
