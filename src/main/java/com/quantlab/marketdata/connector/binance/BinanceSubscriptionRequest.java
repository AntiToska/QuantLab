package com.quantlab.marketdata.connector.binance;

import java.util.List;
import java.util.Objects;

/**
 * Binance WebSocket 订阅请求。
 */
public record BinanceSubscriptionRequest(
        String method,
        List<String> params,
        long id
) {

    public BinanceSubscriptionRequest {
        Objects.requireNonNull(method, "method must not be null");
        Objects.requireNonNull(params, "params must not be null");
        if (method.isBlank()) {
            throw new IllegalArgumentException("method must not be blank");
        }
        if (params.isEmpty()) {
            throw new IllegalArgumentException("params must not be empty");
        }
        if (id <= 0) {
            throw new IllegalArgumentException("id must be positive");
        }

        params = List.copyOf(params);
    }

    public static BinanceSubscriptionRequest subscribe(List<String> streams, long id) {
        return new BinanceSubscriptionRequest("SUBSCRIBE", streams, id);
    }
}
