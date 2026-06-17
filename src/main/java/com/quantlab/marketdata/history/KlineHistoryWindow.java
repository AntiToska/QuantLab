package com.quantlab.marketdata.history;

import java.time.Instant;
import java.util.Objects;

/**
 * 一段可用于回测的 K 线时间窗口。
 */
public record KlineHistoryWindow(
        Instant fromInclusive,
        Instant toExclusive,
        int bars
) {

    public KlineHistoryWindow {
        Objects.requireNonNull(fromInclusive, "fromInclusive must not be null");
        Objects.requireNonNull(toExclusive, "toExclusive must not be null");
        if (!fromInclusive.isBefore(toExclusive)) {
            throw new IllegalArgumentException("fromInclusive must be before toExclusive");
        }
        if (bars <= 0) {
            throw new IllegalArgumentException("bars must be greater than 0");
        }
    }
}
