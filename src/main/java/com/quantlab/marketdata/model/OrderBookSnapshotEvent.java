package com.quantlab.marketdata.model;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

/**
 * 订单簿快照事件。
 * <p>
 * 当前先使用完整快照模型，后续如果需要更高频的数据链路，
 * 再增加增量更新事件。
 */
public record OrderBookSnapshotEvent(
        Instrument instrument,
        Instant eventTime,
        Instant receivedAt,
        long updateId,
        List<OrderBookLevel> bids,
        List<OrderBookLevel> asks
) implements MarketDataEvent {

    public OrderBookSnapshotEvent {
        Objects.requireNonNull(instrument, "instrument must not be null");
        Objects.requireNonNull(eventTime, "eventTime must not be null");
        Objects.requireNonNull(receivedAt, "receivedAt must not be null");
        Objects.requireNonNull(bids, "bids must not be null");
        Objects.requireNonNull(asks, "asks must not be null");
        if (updateId < 0) {
            throw new IllegalArgumentException("updateId must not be negative");
        }

        bids = List.copyOf(bids);
        asks = List.copyOf(asks);
    }

    @Override
    public MarketDataEventType type() {
        return MarketDataEventType.ORDER_BOOK_SNAPSHOT;
    }
}
