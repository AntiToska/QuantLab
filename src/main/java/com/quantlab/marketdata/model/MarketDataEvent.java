package com.quantlab.marketdata.model;

import java.time.Instant;

/**
 * 所有行情事件的统一抽象。
 * <p>
 * 后续无论是 WebSocket 实时采集，还是历史回放，
 * 都应尽量收敛到这一层事件模型之上。
 */
public sealed interface MarketDataEvent permits KlineEvent, OrderBookSnapshotEvent, TradeEvent {

    MarketDataEventType type();

    Instrument instrument();

    Instant eventTime();

    Instant receivedAt();
}
