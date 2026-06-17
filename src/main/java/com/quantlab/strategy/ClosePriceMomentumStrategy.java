package com.quantlab.strategy;

import com.quantlab.marketdata.model.KlineEvent;
import java.math.BigDecimal;

/**
 * 最小收盘价动量策略。
 * <p>
 * 当前收盘价高于上一根 K 线则买入，低于上一根则卖出，否则持有。
 * 这个策略不追求真实收益，只作为回测闭环和指标输出的可运行样例。
 */
public class ClosePriceMomentumStrategy implements KlineStrategy {

    private BigDecimal previousClosePrice;

    @Override
    public String name() {
        return "close-price-momentum";
    }

    @Override
    public StrategySignal onKline(KlineEvent event) {
        if (previousClosePrice == null) {
            previousClosePrice = event.closePrice();
            return StrategySignal.HOLD;
        }

        int comparison = event.closePrice().compareTo(previousClosePrice);
        previousClosePrice = event.closePrice();
        if (comparison > 0) {
            return StrategySignal.BUY;
        }
        if (comparison < 0) {
            return StrategySignal.SELL;
        }
        return StrategySignal.HOLD;
    }
}
