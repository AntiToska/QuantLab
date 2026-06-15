package com.quantlab.marketdata.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

/**
 * K 线事件。
 */
public record KlineEvent(
        Instrument instrument,
        Instant eventTime,
        Instant receivedAt,
        KlineInterval interval,
        Instant openTime,
        Instant closeTime,
        BigDecimal openPrice,
        BigDecimal highPrice,
        BigDecimal lowPrice,
        BigDecimal closePrice,
        BigDecimal volume,
        boolean closed
) implements MarketDataEvent {

    public KlineEvent {
        Objects.requireNonNull(instrument, "instrument must not be null");
        Objects.requireNonNull(eventTime, "eventTime must not be null");
        Objects.requireNonNull(receivedAt, "receivedAt must not be null");
        Objects.requireNonNull(interval, "interval must not be null");
        Objects.requireNonNull(openTime, "openTime must not be null");
        Objects.requireNonNull(closeTime, "closeTime must not be null");
        Objects.requireNonNull(openPrice, "openPrice must not be null");
        Objects.requireNonNull(highPrice, "highPrice must not be null");
        Objects.requireNonNull(lowPrice, "lowPrice must not be null");
        Objects.requireNonNull(closePrice, "closePrice must not be null");
        Objects.requireNonNull(volume, "volume must not be null");

        if (closeTime.isBefore(openTime)) {
            throw new IllegalArgumentException("closeTime must not be before openTime");
        }
        if (openPrice.signum() <= 0 || highPrice.signum() <= 0 || lowPrice.signum() <= 0 || closePrice.signum() <= 0) {
            throw new IllegalArgumentException("prices must be positive");
        }
        if (volume.signum() < 0) {
            throw new IllegalArgumentException("volume must not be negative");
        }
        if (highPrice.compareTo(lowPrice) < 0) {
            throw new IllegalArgumentException("highPrice must be greater than or equal to lowPrice");
        }
    }

    @Override
    public MarketDataEventType type() {
        return MarketDataEventType.KLINE;
    }
}
