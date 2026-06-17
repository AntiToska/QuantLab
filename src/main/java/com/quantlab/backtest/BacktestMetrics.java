package com.quantlab.backtest;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Objects;

/**
 * 回测绩效指标。
 * <p>
 * 这些字段会作为后续 AI Research 的结构化输入，
 * 因此先保持含义清晰、计算口径简单。
 */
public record BacktestMetrics(
        BigDecimal initialEquity,
        BigDecimal finalEquity,
        BigDecimal totalReturn,
        BigDecimal maxDrawdown,
        int executedTrades,
        BigDecimal winRate
) {

    private static final MathContext DIVISION_CONTEXT = MathContext.DECIMAL64;

    public BacktestMetrics {
        Objects.requireNonNull(initialEquity, "initialEquity must not be null");
        Objects.requireNonNull(finalEquity, "finalEquity must not be null");
        Objects.requireNonNull(totalReturn, "totalReturn must not be null");
        Objects.requireNonNull(maxDrawdown, "maxDrawdown must not be null");
        Objects.requireNonNull(winRate, "winRate must not be null");
        if (initialEquity.signum() < 0 || finalEquity.signum() < 0) {
            throw new IllegalArgumentException("equity must not be negative");
        }
        if (maxDrawdown.signum() < 0) {
            throw new IllegalArgumentException("maxDrawdown must not be negative");
        }
        if (executedTrades < 0) {
            throw new IllegalArgumentException("executedTrades must not be negative");
        }
        if (winRate.signum() < 0 || winRate.compareTo(BigDecimal.ONE) > 0) {
            throw new IllegalArgumentException("winRate must be between 0 and 1");
        }
    }

    public static BacktestMetrics empty(BigDecimal initialEquity) {
        return new BacktestMetrics(
                initialEquity,
                initialEquity,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                0,
                BigDecimal.ZERO
        );
    }

    public static BigDecimal returnRate(BigDecimal initialEquity, BigDecimal finalEquity) {
        if (initialEquity.signum() == 0) {
            return BigDecimal.ZERO;
        }
        return finalEquity.subtract(initialEquity).divide(initialEquity, DIVISION_CONTEXT);
    }

    public static BigDecimal ratio(int numerator, int denominator) {
        if (denominator == 0) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(numerator).divide(BigDecimal.valueOf(denominator), DIVISION_CONTEXT);
    }
}
