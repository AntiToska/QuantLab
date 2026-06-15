package com.quantlab.marketdata.connector;

import com.quantlab.marketdata.model.MarketDataEvent;

/**
 * 行情事件发布接口。
 * <p>
 * 当前先作为模块内部边界，后续可以接入事件总线、
 * 存储流水线或策略引擎。
 */
public interface MarketDataEventPublisher {

    void publish(MarketDataEvent event);
}
