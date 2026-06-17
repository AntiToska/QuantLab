package com.quantlab.backtest;

import com.quantlab.marketdata.model.KlineEvent;
import com.quantlab.strategy.StrategySignal;
import java.math.BigDecimal;
import java.util.Objects;

/**
 * 最小组合账户。
 * <p>
 * 当前只支持单资产现金和持仓，用于先跑通回测资金变化。
 */
public class Portfolio {

    private BigDecimal cash;
    private BigDecimal position = BigDecimal.ZERO;
    private BigDecimal lastPrice = BigDecimal.ZERO;

    public Portfolio(BigDecimal initialCash) {
        Objects.requireNonNull(initialCash, "initialCash must not be null");
        if (initialCash.signum() < 0) {
            throw new IllegalArgumentException("initialCash must not be negative");
        }
        this.cash = initialCash;
    }

    public void mark(KlineEvent event) {
        lastPrice = event.closePrice();
    }

    public boolean apply(SimulatedTrade trade) {
        BigDecimal notional = trade.notional();
        if (trade.signal() == StrategySignal.BUY) {
            if (cash.compareTo(notional) < 0) {
                return false;
            }
            cash = cash.subtract(notional);
            position = position.add(trade.quantity());
            return true;
        }
        if (position.compareTo(trade.quantity()) < 0) {
            return false;
        }
        cash = cash.add(notional);
        position = position.subtract(trade.quantity());
        return true;
    }

    public BigDecimal cash() {
        return cash;
    }

    public BigDecimal position() {
        return position;
    }

    public BigDecimal equity() {
        return cash.add(position.multiply(lastPrice));
    }
}
