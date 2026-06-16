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
