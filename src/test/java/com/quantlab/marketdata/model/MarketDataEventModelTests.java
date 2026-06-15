package com.quantlab.marketdata.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;

class MarketDataEventModelTests {

    @Test
    void shouldNormalizeInstrumentSymbol() {
        Instrument instrument = new Instrument(Exchange.BINANCE, " btcusdt ");

        assertThat(instrument.symbol()).isEqualTo("BTCUSDT");
    }

    @Test
    void shouldCreateTradeEvent() {
        TradeEvent event = new TradeEvent(
                new Instrument(Exchange.BINANCE, "BTCUSDT"),
                Instant.parse("2026-06-16T03:00:00Z"),
                Instant.parse("2026-06-16T03:00:01Z"),
                "10001",
                new BigDecimal("105000.12"),
                new BigDecimal("0.01"),
                TradeSide.BUY
        );

        assertThat(event.type()).isEqualTo(MarketDataEventType.TRADE);
        assertThat(event.instrument().exchange()).isEqualTo(Exchange.BINANCE);
    }

    @Test
    void shouldRejectInvalidKlinePriceRange() {
        assertThatThrownBy(() -> new KlineEvent(
                new Instrument(Exchange.OKX, "ETHUSDT"),
                Instant.parse("2026-06-16T03:00:00Z"),
                Instant.parse("2026-06-16T03:00:01Z"),
                KlineInterval.ONE_MINUTE,
                Instant.parse("2026-06-16T03:00:00Z"),
                Instant.parse("2026-06-16T03:00:59Z"),
                new BigDecimal("100"),
                new BigDecimal("99"),
                new BigDecimal("101"),
                new BigDecimal("100"),
                new BigDecimal("12.5"),
                true
        )).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("highPrice");
    }

    @Test
    void shouldCopyOrderBookLevels() {
        OrderBookSnapshotEvent event = new OrderBookSnapshotEvent(
                new Instrument(Exchange.BYBIT, "BTCUSDT"),
                Instant.parse("2026-06-16T03:00:00Z"),
                Instant.parse("2026-06-16T03:00:01Z"),
                42L,
                List.of(new OrderBookLevel(new BigDecimal("100"), new BigDecimal("1.2"))),
                List.of(new OrderBookLevel(new BigDecimal("101"), new BigDecimal("0.8")))
        );

        assertThat(event.type()).isEqualTo(MarketDataEventType.ORDER_BOOK_SNAPSHOT);
        assertThat(event.bids()).hasSize(1);
        assertThatThrownBy(() -> event.bids().add(new OrderBookLevel(BigDecimal.ONE, BigDecimal.ONE)))
                .isInstanceOf(UnsupportedOperationException.class);
    }
}
