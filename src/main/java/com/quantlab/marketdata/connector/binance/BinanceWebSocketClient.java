package com.quantlab.marketdata.connector.binance;

/**
 * Binance WebSocket 客户端抽象。
 * <p>
 * 负责建立连接并把原始消息回调给上层连接器。
 */
public interface BinanceWebSocketClient {

    BinanceWebSocketSession connect(BinanceWebSocketListener listener);
}
