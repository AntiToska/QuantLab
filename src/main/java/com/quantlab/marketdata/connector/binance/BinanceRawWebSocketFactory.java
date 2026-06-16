package com.quantlab.marketdata.connector.binance;

import java.net.URI;

/**
 * 原始 WebSocket 创建工厂。
 */
public interface BinanceRawWebSocketFactory {

    BinanceRawWebSocket connect(URI uri, BinanceWebSocketListener listener);
}
