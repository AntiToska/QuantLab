package com.quantlab.backtest;

import java.time.Instant;
import java.util.Objects;

/**
 * 最小回测结果。
 * <p>
 * 当前只统计事件循环是否正确跑完，为后续收益、回撤等指标预留位置。
 */
public record BacktestResult(
        String strategyName,
        Instant fromInclusive,
        Instant toExclusive,
        int processedBars,
        int buySignals,
        int sellSignals,
        int holdSignals
) {

    public BacktestResult {
        if (strategyName == null || strategyName.isBlank()) {
            throw new IllegalArgumentException("strategyName must not be blank");
        }
        Objects.requireNonNull(fromInclusive, "fromInclusive must not be null");
        Objects.requireNonNull(toExclusive, "toExclusive must not be null");
        if (!fromInclusive.isBefore(toExclusive)) {
            throw new IllegalArgumentException("fromInclusive must be before toExclusive");
        }
        if (processedBars < 0 || buySignals < 0 || sellSignals < 0 || holdSignals < 0) {
            throw new IllegalArgumentException("signal counts must not be negative");
        }
        if (processedBars != buySignals + sellSignals + holdSignals) {
            throw new IllegalArgumentException("processedBars must equal signal counts");
        }

        strategyName = strategyName.trim();
    }
}
