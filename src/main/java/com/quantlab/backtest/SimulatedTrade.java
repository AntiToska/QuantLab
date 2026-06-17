package com.quantlab.backtest;

import com.quantlab.marketdata.model.Instrument;
import com.quantlab.strategy.StrategySignal;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

/**
 * 回测中的模拟成交。
 */
public record SimulatedTrade(
        Instrument instrument,
        StrategySignal signal,
        BigDecimal quantity,
        BigDecimal price,
        Instant eventTime
) {

    public SimulatedTrade {
        Objects.requireNonNull(instrument, "instrument must not be null");
        Objects.requireNonNull(signal, "signal must not be null");
        Objects.requireNonNull(quantity, "quantity must not be null");
        Objects.requireNonNull(price, "price must not be null");
        Objects.requireNonNull(eventTime, "eventTime must not be null");
        if (signal == StrategySignal.HOLD) {
            throw new IllegalArgumentException("hold signal cannot become a trade");
        }
        if (quantity.signum() <= 0) {
            throw new IllegalArgumentException("quantity must be positive");
        }
        if (price.signum() <= 0) {
            throw new IllegalArgumentException("price must be positive");
        }
    }

    public BigDecimal notional() {
        return price.multiply(quantity);
    }
}
