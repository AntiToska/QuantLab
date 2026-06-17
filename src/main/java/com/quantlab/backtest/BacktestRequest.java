package com.quantlab.backtest;

import com.quantlab.marketdata.model.Instrument;
import com.quantlab.marketdata.model.KlineInterval;
import java.time.Instant;
import java.util.Objects;

/**
 * 回测请求。
 * <p>
 * 当前只包含单标的、单周期、固定时间窗口，
 * 先服务最小 K 线回测闭环。
 */
public record BacktestRequest(
        Instrument instrument,
        KlineInterval interval,
        Instant fromInclusive,
        Instant toExclusive
) {

    public BacktestRequest {
        Objects.requireNonNull(instrument, "instrument must not be null");
        Objects.requireNonNull(interval, "interval must not be null");
        Objects.requireNonNull(fromInclusive, "fromInclusive must not be null");
        Objects.requireNonNull(toExclusive, "toExclusive must not be null");
        if (!fromInclusive.isBefore(toExclusive)) {
            throw new IllegalArgumentException("fromInclusive must be before toExclusive");
        }
    }
}
