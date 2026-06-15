package com.quantlab.marketdata.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

/**
 * 成交事件。
 */
public record TradeEvent(
        Instrument instrument,
        Instant eventTime,
        Instant receivedAt,
        String tradeId,
        BigDecimal price,
        BigDecimal quantity,
        TradeSide side
) implements MarketDataEvent {

    public TradeEvent {
        Objects.requireNonNull(instrument, "instrument must not be null");
        Objects.requireNonNull(eventTime, "eventTime must not be null");
        Objects.requireNonNull(receivedAt, "receivedAt must not be null");
        Objects.requireNonNull(price, "price must not be null");
        Objects.requireNonNull(quantity, "quantity must not be null");
        Objects.requireNonNull(side, "side must not be null");

        if (tradeId == null || tradeId.isBlank()) {
            throw new IllegalArgumentException("tradeId must not be blank");
        }
        if (price.signum() <= 0) {
            throw new IllegalArgumentException("price must be positive");
        }
        if (quantity.signum() <= 0) {
            throw new IllegalArgumentException("quantity must be positive");
        }

        tradeId = tradeId.trim();
    }

    @Override
    public MarketDataEventType type() {
        return MarketDataEventType.TRADE;
    }
}
