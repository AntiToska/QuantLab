package com.quantlab.marketdata.connector;

import static org.assertj.core.api.Assertions.assertThat;

import com.quantlab.marketdata.model.Exchange;
import com.quantlab.marketdata.model.Instrument;
import com.quantlab.marketdata.model.KlineEvent;
import com.quantlab.marketdata.model.KlineInterval;
import com.quantlab.marketdata.model.OrderBookLevel;
import com.quantlab.marketdata.model.OrderBookSnapshotEvent;
import com.quantlab.marketdata.model.TradeEvent;
import com.quantlab.marketdata.model.TradeSide;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;

class JdbcMarketDataEventPublisherTests {

    @Test
    void shouldPersistTradeEvent() throws Exception {
        String jdbcUrl = jdbcUrl("trade");
        JdbcMarketDataEventPublisher publisher = new JdbcMarketDataEventPublisher(jdbcUrl, "sa", "");

        publisher.publish(new TradeEvent(
                new Instrument(Exchange.BINANCE, "BTCUSDT"),
                Instant.parse("2026-06-17T00:00:01Z"),
                Instant.parse("2026-06-17T00:00:02Z"),
                "12345",
                new BigDecimal("100000.12"),
                new BigDecimal("0.015"),
                TradeSide.BUY
        ));

        assertThat(queryCount(jdbcUrl, "select count(*) from market_data_trades where symbol = 'BTCUSDT' and trade_id = '12345'"))
                .isEqualTo(1);
    }

    @Test
    void shouldPersistKlineEvent() throws Exception {
        String jdbcUrl = jdbcUrl("kline");
        JdbcMarketDataEventPublisher publisher = new JdbcMarketDataEventPublisher(jdbcUrl, "sa", "");

        publisher.publish(new KlineEvent(
                new Instrument(Exchange.BINANCE, "ETHUSDT"),
                Instant.parse("2026-06-17T00:01:00Z"),
                Instant.parse("2026-06-17T00:01:01Z"),
                KlineInterval.ONE_MINUTE,
                Instant.parse("2026-06-17T00:00:00Z"),
                Instant.parse("2026-06-17T00:00:59Z"),
                new BigDecimal("2500.10"),
                new BigDecimal("2510.50"),
                new BigDecimal("2498.90"),
                new BigDecimal("2508.80"),
                new BigDecimal("12.345"),
                true
        ));

        assertThat(queryCount(jdbcUrl, "select count(*) from market_data_klines where symbol = 'ETHUSDT' and interval_name = 'ONE_MINUTE'"))
                .isEqualTo(1);
    }

    @Test
    void shouldIgnoreOrderBookSnapshotForNow() throws Exception {
        String jdbcUrl = jdbcUrl("orderbook");
        JdbcMarketDataEventPublisher publisher = new JdbcMarketDataEventPublisher(jdbcUrl, "sa", "");

        publisher.publish(new OrderBookSnapshotEvent(
                new Instrument(Exchange.BINANCE, "BTCUSDT"),
                Instant.parse("2026-06-17T00:00:01Z"),
                Instant.parse("2026-06-17T00:00:02Z"),
                10L,
                List.of(new OrderBookLevel(new BigDecimal("100"), new BigDecimal("1"))),
                List.of(new OrderBookLevel(new BigDecimal("101"), new BigDecimal("2")))
        ));

        assertThat(queryCount(jdbcUrl, "select count(*) from market_data_trades")).isZero();
        assertThat(queryCount(jdbcUrl, "select count(*) from market_data_klines")).isZero();
    }

    private String jdbcUrl(String databaseName) {
        return "jdbc:h2:mem:" + databaseName + ";MODE=PostgreSQL;DB_CLOSE_DELAY=-1";
    }

    private int queryCount(String jdbcUrl, String sql) throws Exception {
        try (Connection connection = DriverManager.getConnection(jdbcUrl, "sa", "");
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
            resultSet.next();
            return resultSet.getInt(1);
        }
    }
}
