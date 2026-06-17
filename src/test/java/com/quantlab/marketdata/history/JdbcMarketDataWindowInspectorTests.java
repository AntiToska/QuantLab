package com.quantlab.marketdata.history;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.quantlab.marketdata.connector.JdbcMarketDataEventPublisher;
import com.quantlab.marketdata.model.Exchange;
import com.quantlab.marketdata.model.Instrument;
import com.quantlab.marketdata.model.KlineEvent;
import com.quantlab.marketdata.model.KlineInterval;
import java.math.BigDecimal;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class JdbcMarketDataWindowInspectorTests {

    @Test
    void shouldResolveLatestKlineWindow() {
        String jdbcUrl = jdbcUrl("window_inspector");
        JdbcMarketDataEventPublisher publisher = new JdbcMarketDataEventPublisher(jdbcUrl, "sa", "");
        JdbcMarketDataWindowInspector inspector = new JdbcMarketDataWindowInspector(jdbcUrl, "sa", "");
        Instrument instrument = new Instrument(Exchange.BINANCE, "BTCUSDT");

        publisher.publish(kline(instrument, "2026-06-17T00:00:00Z", "100", "101"));
        publisher.publish(kline(instrument, "2026-06-17T00:01:00Z", "101", "102"));
        publisher.publish(kline(instrument, "2026-06-17T00:02:00Z", "102", "103"));

        KlineHistoryWindow window = inspector.latestKlineWindow(instrument, KlineInterval.ONE_MINUTE, 2).orElseThrow();

        assertThat(window.fromInclusive()).isEqualTo(Instant.parse("2026-06-17T00:01:00Z"));
        assertThat(window.toExclusive()).isEqualTo(Instant.parse("2026-06-17T00:03:00Z"));
        assertThat(window.bars()).isEqualTo(2);
    }

    @Test
    void shouldRejectNonPositiveBars() {
        JdbcMarketDataWindowInspector inspector = new JdbcMarketDataWindowInspector(jdbcUrl("window_invalid"), "sa", "");

        assertThatThrownBy(() -> inspector.latestKlineWindow(
                new Instrument(Exchange.BINANCE, "BTCUSDT"),
                KlineInterval.ONE_MINUTE,
                0
        )).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("bars");
    }

    private KlineEvent kline(Instrument instrument, String openTime, String openPrice, String closePrice) {
        Instant open = Instant.parse(openTime);
        BigDecimal openValue = new BigDecimal(openPrice);
        BigDecimal closeValue = new BigDecimal(closePrice);
        return new KlineEvent(
                instrument,
                open.plusSeconds(59),
                open.plusSeconds(60),
                KlineInterval.ONE_MINUTE,
                open,
                open.plusSeconds(59),
                openValue,
                openValue.max(closeValue),
                openValue.min(closeValue),
                closeValue,
                new BigDecimal("10.0"),
                true
        );
    }

    private String jdbcUrl(String databaseName) {
        return "jdbc:h2:mem:" + databaseName + ";MODE=PostgreSQL;DB_CLOSE_DELAY=-1";
    }
}
