package com.quantlab.marketdata.connector.binance;

/**
 * 原始 WebSocket 连接适配接口。
 * <p>
 * 这一层只暴露最小的文本发送与关闭能力，
 * 方便把 JDK WebSocket 与业务层 session 解耦。
 */
public interface BinanceRawWebSocket {

    void sendText(String payload);

    void abort();
}
