package com.quantlab.marketdata.connector.binance;

/**
 * Binance 原始消息的统一封装。
 */
public record BinancePayload(
        BinanceChannel channel,
        BinanceTradeMessage trade,
        BinanceKlineMessage kline
) {

    /**
     * 构造控制类消息载荷。
     * <p>
     * 用于订阅确认、pong 等不参与业务事件映射的消息。
     */
    public static BinancePayload control() {
        return new BinancePayload(null, null, null);
    }

    /**
     * 当前 payload 是否属于控制类消息。
     */
    public boolean controlMessage() {
        return channel == null;
    }

    /**
     * 构造 trade 类型载荷。
     */
    public static BinancePayload trade(BinanceTradeMessage trade) {
        return new BinancePayload(BinanceChannel.TRADE, trade, null);
    }

    /**
     * 构造 kline 类型载荷。
     */
    public static BinancePayload kline(BinanceKlineMessage kline) {
        return new BinancePayload(BinanceChannel.KLINE, null, kline);
    }
}
