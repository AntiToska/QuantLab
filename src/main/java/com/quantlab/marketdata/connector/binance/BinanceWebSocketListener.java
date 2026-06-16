package com.quantlab.marketdata.connector.binance;

/**
 * Binance WebSocket 事件监听器。
 * <p>
 * 统一承接消息、异常和连接关闭事件，避免传输层直接依赖连接器实现细节。
 */
public interface BinanceWebSocketListener {

    void onMessage(String payload);

    void onError(Throwable throwable);

    void onClosed();
}
