package com.quantlab.marketdata.connector.binance;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Binance kline 消息 DTO。
 */
public record BinanceKlineMessage(
        @JsonProperty("e") String eventType,
        @JsonProperty("E") long eventTime,
        @JsonProperty("s") String symbol,
        @JsonProperty("k") KlineData kline
) {

    /**
     * K 线内部对象。
     */
    public record KlineData(
            @JsonProperty("t") long openTime,
            @JsonProperty("T") long closeTime,
            @JsonProperty("i") String interval,
            @JsonProperty("o") String openPrice,
            @JsonProperty("c") String closePrice,
            @JsonProperty("h") String highPrice,
            @JsonProperty("l") String lowPrice,
            @JsonProperty("v") String baseAssetVolume,
            @JsonProperty("x") boolean closed
    ) {
    }
}
