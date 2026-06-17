package com.quantlab.marketdata.connector.binance;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Binance trade 消息 DTO。
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record BinanceTradeMessage(
        @JsonProperty("e") String eventType,
        @JsonProperty("E") long eventTime,
        @JsonProperty("s") String symbol,
        @JsonProperty("t") long tradeId,
        @JsonProperty("p") String price,
        @JsonProperty("q") String quantity,
        @JsonProperty("m") boolean isBuyerMaker
) {
}
