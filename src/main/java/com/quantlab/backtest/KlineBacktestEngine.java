package com.quantlab.backtest;

import com.quantlab.marketdata.history.MarketDataHistoryReader;
import com.quantlab.marketdata.model.KlineEvent;
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
        for (KlineEvent event : events) {
            StrategySignal signal = strategy.onKline(event);
            if (signal == null) {
                throw new IllegalStateException("strategy signal must not be null");
            }
            switch (signal) {
                case BUY -> buySignals++;
                case SELL -> sellSignals++;
                case HOLD -> holdSignals++;
            }
        }

        return new BacktestResult(
                strategy.name(),
                request.fromInclusive(),
                request.toExclusive(),
                events.size(),
                buySignals,
                sellSignals,
                holdSignals
        );
    }
}
