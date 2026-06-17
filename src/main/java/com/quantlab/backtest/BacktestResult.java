package com.quantlab.backtest;

import com.quantlab.marketdata.model.Instrument;
import com.quantlab.marketdata.model.KlineInterval;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

/**
 * 最小回测结果。
 * <p>
 * 当前只统计事件循环是否正确跑完，为后续收益、回撤等指标预留位置。
 */
public record BacktestResult(
        String strategyName,
        Instrument instrument,
        KlineInterval interval,
        Instant fromInclusive,
        Instant toExclusive,
        BigDecimal initialCash,
        BigDecimal tradeQuantity,
        int processedBars,
        int buySignals,
        int sellSignals,
        int holdSignals,
        BigDecimal finalCash,
        BigDecimal finalPosition,
        BigDecimal finalEquity,
        BacktestMetrics metrics
) {

    public BacktestResult {
        if (strategyName == null || strategyName.isBlank()) {
            throw new IllegalArgumentException("strategyName must not be blank");
        }
        Objects.requireNonNull(instrument, "instrument must not be null");
        Objects.requireNonNull(interval, "interval must not be null");
        Objects.requireNonNull(fromInclusive, "fromInclusive must not be null");
        Objects.requireNonNull(toExclusive, "toExclusive must not be null");
        Objects.requireNonNull(initialCash, "initialCash must not be null");
        Objects.requireNonNull(tradeQuantity, "tradeQuantity must not be null");
        Objects.requireNonNull(finalCash, "finalCash must not be null");
        Objects.requireNonNull(finalPosition, "finalPosition must not be null");
        Objects.requireNonNull(finalEquity, "finalEquity must not be null");
        Objects.requireNonNull(metrics, "metrics must not be null");
        if (!fromInclusive.isBefore(toExclusive)) {
            throw new IllegalArgumentException("fromInclusive must be before toExclusive");
        }
        if (processedBars < 0 || buySignals < 0 || sellSignals < 0 || holdSignals < 0) {
            throw new IllegalArgumentException("signal counts must not be negative");
        }
        if (processedBars != buySignals + sellSignals + holdSignals) {
            throw new IllegalArgumentException("processedBars must equal signal counts");
        }
        if (finalPosition.signum() < 0) {
            throw new IllegalArgumentException("finalPosition must not be negative");
        }
        if (initialCash.signum() < 0) {
            throw new IllegalArgumentException("initialCash must not be negative");
        }
        if (tradeQuantity.signum() <= 0) {
            throw new IllegalArgumentException("tradeQuantity must be positive");
        }

        strategyName = strategyName.trim();
    }
}
