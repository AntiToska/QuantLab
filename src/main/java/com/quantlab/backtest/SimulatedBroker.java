package com.quantlab.backtest;

import com.quantlab.marketdata.model.KlineEvent;
import com.quantlab.strategy.StrategySignal;
import java.math.BigDecimal;
import java.util.Optional;

/**
 * 最小模拟经纪商。
 * <p>
 * 当前按 K 线收盘价立即成交，不模拟手续费和滑点。
 */
public class SimulatedBroker {

    public Optional<SimulatedOrder> createOrder(
            KlineEvent event,
            StrategySignal signal,
            BigDecimal tradeQuantity
    ) {
        if (signal == StrategySignal.HOLD) {
            return Optional.empty();
        }
        return Optional.of(new SimulatedOrder(
                event.instrument(),
                signal,
                tradeQuantity,
                event.closePrice(),
                event.eventTime()
        ));
    }

    public SimulatedTrade execute(SimulatedOrder order) {
        return new SimulatedTrade(
                order.instrument(),
                order.signal(),
                order.quantity(),
                order.price(),
                order.eventTime()
        );
    }
}
