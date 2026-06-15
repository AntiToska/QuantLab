package com.quantlab.marketdata.connector.binance;

/**
 * Binance 原始消息的统一封装。
 */
public record BinancePayload(
        BinanceChannel channel,
        BinanceTradeMessage trade,
        BinanceKlineMessage kline
) {

    public static BinancePayload trade(BinanceTradeMessage trade) {
        return new BinancePayload(BinanceChannel.TRADE, trade, null);
    }

    public static BinancePayload kline(BinanceKlineMessage kline) {
        return new BinancePayload(BinanceChannel.KLINE, null, kline);
    }
}
