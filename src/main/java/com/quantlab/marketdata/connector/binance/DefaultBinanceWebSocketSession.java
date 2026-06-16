package com.quantlab.marketdata.connector.binance;

/**
 * 默认 Binance WebSocket session 实现。
 */
public class DefaultBinanceWebSocketSession implements BinanceWebSocketSession {

    private final BinanceRawWebSocket webSocket;
    private final BinanceSubscriptionRequestSerializer serializer;
    private BinanceSessionState state = BinanceSessionState.OPEN;

    public DefaultBinanceWebSocketSession(
            BinanceRawWebSocket webSocket,
            BinanceSubscriptionRequestSerializer serializer
    ) {
        this.webSocket = webSocket;
        this.serializer = serializer;
    }

    @Override
    public void send(BinanceSubscriptionRequest request) {
        webSocket.sendText(serializer.serialize(request));
    }

    @Override
    public BinanceSessionState state() {
        return state;
    }

    @Override
    public void close() {
        state = BinanceSessionState.CLOSED;
        webSocket.abort();
    }
}
