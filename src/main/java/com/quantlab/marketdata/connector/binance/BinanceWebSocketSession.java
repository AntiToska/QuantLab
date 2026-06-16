package com.quantlab.marketdata.connector.binance;

/**
 * Binance WebSocket 会话抽象。
 * <p>
 * 真实网络层接入后，连接器只依赖这个最小能力集。
 */
public interface BinanceWebSocketSession {

    void send(BinanceSubscriptionRequest request);

    BinanceSessionState state();

    void close();
}
