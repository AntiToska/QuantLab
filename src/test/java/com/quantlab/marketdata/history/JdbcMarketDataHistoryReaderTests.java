package com.quantlab.marketdata.history;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.quantlab.marketdata.connector.JdbcMarketDataEventPublisher;
import com.quantlab.marketdata.model.Exchange;
import com.quantlab.marketdata.model.Instrument;
import com.quantlab.marketdata.model.KlineEvent;
import com.quantlab.marketdata.model.KlineInterval;
import com.quantlab.marketdata.model.TradeEvent;
import com.quantlab.marketdata.model.TradeSide;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;

class JdbcMarketDataHistoryReaderTests {

    @Test
    void shouldLoadTradesByInstrumentAndTimeRange() {
        String jdbcUrl = jdbcUrl("history_trades");
        JdbcMarketDataEventPublisher publisher = new JdbcMarketDataEventPublisher(jdbcUrl, "sa", "");
        JdbcMarketDataHistoryReader reader = new JdbcMarketDataHistoryReader(jdbcUrl, "sa", "");
        Instrument instrument = new Instrument(Exchange.BINANCE, "BTCUSDT");

        publisher.publish(trade(instrument, "1", "2026-06-17T00:00:01Z"));
        publisher.publish(trade(instrument, "2", "2026-06-17T00:00:02Z"));
        publisher.publish(trade(new Instrument(Exchange.BINANCE, "ETHUSDT"), "3", "2026-06-17T00:00:02Z"));

        List<TradeEvent> events = reader.loadTrades(
                instrument,
                Instant.parse("2026-06-17T00:00:00Z"),
                Instant.parse("2026-06-17T00:00:03Z")
        );

        assertThat(events)
                .extracting(TradeEvent::tradeId)
                .containsExactly("1", "2");
    }

    @Test
    void shouldLoadKlinesByInstrumentIntervalAndOpenTimeRange() {
        String jdbcUrl = jdbcUrl("history_klines");
        JdbcMarketDataEventPublisher publisher = new JdbcMarketDataEventPublisher(jdbcUrl, "sa", "");
        JdbcMarketDataHistoryReader reader = new JdbcMarketDataHistoryReader(jdbcUrl, "sa", "");
        Instrument instrument = new Instrument(Exchange.BINANCE, "BTCUSDT");

        publisher.publish(kline(instrument, KlineInterval.ONE_MINUTE, "2026-06-17T00:00:00Z"));
        publisher.publish(kline(instrument, KlineInterval.ONE_MINUTE, "2026-06-17T00:01:00Z"));
        publisher.publish(kline(instrument, KlineInterval.FIVE_MINUTES, "2026-06-17T00:00:00Z"));

        List<KlineEvent> events = reader.loadKlines(
                instrument,
                KlineInterval.ONE_MINUTE,
                Instant.parse("2026-06-17T00:00:00Z"),
                Instant.parse("2026-06-17T00:02:00Z")
        );

        assertThat(events)
                .extracting(KlineEvent::openTime)
                .containsExactly(
                        Instant.parse("2026-06-17T00:00:00Z"),
                        Instant.parse("2026-06-17T00:01:00Z")
                );
    }

    @Test
    void shouldRejectInvalidTimeRange() {
        JdbcMarketDataHistoryReader reader = new JdbcMarketDataHistoryReader(jdbcUrl("invalid_range"), "sa", "");

        assertThatThrownBy(() -> reader.loadTrades(
                new Instrument(Exchange.BINANCE, "BTCUSDT"),
                Instant.parse("2026-06-17T00:00:00Z"),
                Instant.parse("2026-06-17T00:00:00Z")
        )).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("fromInclusive");
    }

    private TradeEvent trade(Instrument instrument, String tradeId, String eventTime) {
        Instant eventInstant = Instant.parse(eventTime);
        return new TradeEvent(
                instrument,
                eventInstant,
                eventInstant.plusMillis(100),
                tradeId,
                new BigDecimal("100.10"),
                new BigDecimal("0.20"),
                TradeSide.BUY
        );
    }

    private KlineEvent kline(Instrument instrument, KlineInterval interval, String openTime) {
        Instant open = Instant.parse(openTime);
        return new KlineEvent(
                instrument,
                open.plusSeconds(59),
                open.plusSeconds(60),
                interval,
                open,
                open.plusSeconds(59),
                new BigDecimal("100.00"),
                new BigDecimal("105.00"),
                new BigDecimal("99.00"),
                new BigDecimal("102.00"),
                new BigDecimal("10.50"),
                true
        );
    }

    private String jdbcUrl(String databaseName) {
        return "jdbc:h2:mem:" + databaseName + ";MODE=PostgreSQL;DB_CLOSE_DELAY=-1";
    }
}
