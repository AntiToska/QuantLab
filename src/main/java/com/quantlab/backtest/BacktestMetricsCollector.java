package com.quantlab.backtest;

import com.quantlab.strategy.StrategySignal;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.List;

/**
 * 回测指标收集器。
 * <p>
 * 引擎只负责驱动事件，这里集中维护权益曲线、最大回撤和成交统计。
 */
final class BacktestMetricsCollector {

    private static final MathContext DIVISION_CONTEXT = MathContext.DECIMAL64;

    private final BigDecimal initialEquity;
    private final int annualizationPeriods;
    private BigDecimal latestEquity;
    private BigDecimal peakEquity;
    private BigDecimal maxDrawdown = BigDecimal.ZERO;
    private int executedTrades;
    private int sellTrades;
    private int winningSellTrades;
    private BigDecimal openCostBasis = BigDecimal.ZERO;
    private BigDecimal openPosition = BigDecimal.ZERO;
    private final List<BigDecimal> periodReturns = new ArrayList<>();

    BacktestMetricsCollector(BigDecimal initialEquity, int annualizationPeriods) {
        this.initialEquity = initialEquity;
        this.annualizationPeriods = annualizationPeriods;
        this.latestEquity = initialEquity;
        this.peakEquity = initialEquity;
    }

    void recordEquity(BigDecimal equity) {
        if (latestEquity.signum() > 0) {
            periodReturns.add(equity.subtract(latestEquity).divide(latestEquity, DIVISION_CONTEXT));
        }
        latestEquity = equity;
        if (equity.compareTo(peakEquity) > 0) {
            peakEquity = equity;
        }
        if (peakEquity.signum() == 0) {
            return;
        }
        BigDecimal drawdown = peakEquity.subtract(equity).divide(peakEquity, java.math.MathContext.DECIMAL64);
        if (drawdown.compareTo(maxDrawdown) > 0) {
            maxDrawdown = drawdown;
        }
    }

    void recordTrade(SimulatedTrade trade) {
        executedTrades++;
        if (trade.signal() == StrategySignal.BUY) {
            openCostBasis = openCostBasis.add(trade.notional());
            openPosition = openPosition.add(trade.quantity());
            return;
        }

        sellTrades++;
        BigDecimal averageCost = averageOpenCost();
        BigDecimal realizedCost = averageCost.multiply(trade.quantity());
        if (trade.notional().compareTo(realizedCost) > 0) {
            winningSellTrades++;
        }
        openCostBasis = openCostBasis.subtract(realizedCost).max(BigDecimal.ZERO);
        openPosition = openPosition.subtract(trade.quantity()).max(BigDecimal.ZERO);
    }

    BacktestMetrics toMetrics() {
        return new BacktestMetrics(
                initialEquity,
                latestEquity,
                BacktestMetrics.returnRate(initialEquity, latestEquity),
                sharpeRatio(),
                maxDrawdown,
                executedTrades,
                BacktestMetrics.ratio(winningSellTrades, sellTrades)
        );
    }

    private BigDecimal averageOpenCost() {
        if (openPosition.signum() == 0) {
            return BigDecimal.ZERO;
        }
        return openCostBasis.divide(openPosition, DIVISION_CONTEXT);
    }

    private BigDecimal sharpeRatio() {
        if (periodReturns.size() < 2) {
            return BigDecimal.ZERO;
        }

        double mean = periodReturns.stream()
                .mapToDouble(BigDecimal::doubleValue)
                .average()
                .orElse(0.0D);
        double squaredDeviationSum = periodReturns.stream()
                .mapToDouble(value -> {
                    double deviation = value.doubleValue() - mean;
                    return deviation * deviation;
                })
                .sum();
        double standardDeviation = Math.sqrt(squaredDeviationSum / (periodReturns.size() - 1));
        if (standardDeviation == 0.0D) {
            return BigDecimal.ZERO;
        }

        double annualizedSharpe = (mean / standardDeviation) * Math.sqrt(annualizationPeriods);
        return BigDecimal.valueOf(annualizedSharpe);
    }
}
