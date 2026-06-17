package com.quantlab.backtest;

import com.quantlab.marketdata.history.MarketDataHistoryReader;
import com.quantlab.marketdata.model.KlineEvent;
import com.quantlab.marketdata.model.KlineInterval;
import com.quantlab.strategy.KlineStrategy;
import com.quantlab.strategy.StrategySignal;
import java.util.List;
import java.util.Objects;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;

/**
 * K 线回测引擎。
 * <p>
 * 当前只负责从 Market Data 历史读取入口加载 K 线，
 * 并按时间顺序驱动策略执行。
 */
@Service
@ConditionalOnBean(MarketDataHistoryReader.class)
public class KlineBacktestEngine {

    private final MarketDataHistoryReader historyReader;

    public KlineBacktestEngine(MarketDataHistoryReader historyReader) {
        this.historyReader = historyReader;
    }

    public BacktestResult run(BacktestRequest request, KlineStrategy strategy) {
        Objects.requireNonNull(request, "request must not be null");
        Objects.requireNonNull(strategy, "strategy must not be null");

        List<KlineEvent> events = historyReader.loadKlines(
                request.instrument(),
                request.interval(),
                request.fromInclusive(),
                request.toExclusive()
        );

        int buySignals = 0;
        int sellSignals = 0;
        int holdSignals = 0;
        SimulatedBroker broker = new SimulatedBroker();
        Portfolio portfolio = new Portfolio(request.initialCash());
        BacktestMetricsCollector metricsCollector = new BacktestMetricsCollector(
                request.initialCash(),
                annualizationPeriods(request.interval())
        );
        for (KlineEvent event : events) {
            portfolio.mark(event);
            StrategySignal signal = strategy.onKline(event);
            if (signal == null) {
                throw new IllegalStateException("strategy signal must not be null");
            }
            switch (signal) {
                case BUY -> buySignals++;
                case SELL -> sellSignals++;
                case HOLD -> holdSignals++;
            }
            broker.createOrder(event, signal, request.tradeQuantity())
                    .map(broker::execute)
                    .ifPresent(trade -> {
                        if (portfolio.apply(trade)) {
                            metricsCollector.recordTrade(trade);
                        }
                    });
            metricsCollector.recordEquity(portfolio.equity());
        }

        BacktestMetrics metrics = events.isEmpty()
                ? BacktestMetrics.empty(request.initialCash())
                : metricsCollector.toMetrics();
        return new BacktestResult(
                strategy.name(),
                request.instrument(),
                request.interval(),
                request.fromInclusive(),
                request.toExclusive(),
                request.initialCash(),
                request.tradeQuantity(),
                events.size(),
                buySignals,
                sellSignals,
                holdSignals,
                portfolio.cash(),
                portfolio.position(),
                portfolio.equity(),
                metrics
        );
    }

    private int annualizationPeriods(KlineInterval interval) {
        return switch (interval) {
            case ONE_MINUTE -> 365 * 24 * 60;
            case FIVE_MINUTES -> 365 * 24 * 12;
            case FIFTEEN_MINUTES -> 365 * 24 * 4;
            case ONE_HOUR -> 365 * 24;
            case FOUR_HOURS -> 365 * 6;
            case ONE_DAY -> 365;
        };
    }
}
