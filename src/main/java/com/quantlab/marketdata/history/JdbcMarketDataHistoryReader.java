package com.quantlab.marketdata.history;

import com.quantlab.marketdata.model.Exchange;
import com.quantlab.marketdata.model.Instrument;
import com.quantlab.marketdata.model.KlineEvent;
import com.quantlab.marketdata.model.KlineInterval;
import com.quantlab.marketdata.model.TradeEvent;
import com.quantlab.marketdata.model.TradeSide;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * 基于 JDBC 的历史行情读取实现。
 * <p>
 * 当前只读取已经落库的 Trade / Kline 数据，
 * 为下一阶段回测引擎提供最小历史数据入口。
 */
@Component
@ConditionalOnProperty(prefix = "quantlab.market-data.persistence", name = "enabled", havingValue = "true")
public class JdbcMarketDataHistoryReader implements MarketDataHistoryReader {

    private final String jdbcUrl;
    private final String username;
    private final String password;

    public JdbcMarketDataHistoryReader(
            @Value("${quantlab.market-data.persistence.jdbc-url}") String jdbcUrl,
            @Value("${quantlab.market-data.persistence.username}") String username,
            @Value("${quantlab.market-data.persistence.password}") String password
    ) {
        this.jdbcUrl = jdbcUrl;
        this.username = username;
        this.password = password;
    }

    @Override
    public List<TradeEvent> loadTrades(Instrument instrument, Instant fromInclusive, Instant toExclusive) {
        requireTimeRange(fromInclusive, toExclusive);
        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement(
                     """
                     select exchange_name, symbol, event_time, received_at, trade_id, price, quantity, side
                     from market_data_trades
                     where exchange_name = ?
                       and symbol = ?
                       and event_time >= ?
                       and event_time < ?
                     order by event_time asc, id asc
                     """
             )) {
            statement.setString(1, instrument.exchange().name());
            statement.setString(2, instrument.symbol());
            statement.setObject(3, timestamp(fromInclusive));
            statement.setObject(4, timestamp(toExclusive));
            try (ResultSet resultSet = statement.executeQuery()) {
                List<TradeEvent> events = new ArrayList<>();
                while (resultSet.next()) {
                    events.add(toTradeEvent(resultSet));
                }
                return events;
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("failed to load trade history", exception);
        }
    }

    @Override
    public List<KlineEvent> loadKlines(
            Instrument instrument,
            KlineInterval interval,
            Instant fromInclusive,
            Instant toExclusive
    ) {
        requireTimeRange(fromInclusive, toExclusive);
        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement(
                     """
                     select exchange_name,
                            symbol,
                            interval_name,
                            event_time,
                            received_at,
                            open_time,
                            close_time,
                            open_price,
                            high_price,
                            low_price,
                            close_price,
                            volume,
                            closed
                     from market_data_klines
                     where exchange_name = ?
                       and symbol = ?
                       and interval_name = ?
                       and open_time >= ?
                       and open_time < ?
                     order by open_time asc, id asc
                     """
             )) {
            statement.setString(1, instrument.exchange().name());
            statement.setString(2, instrument.symbol());
            statement.setString(3, interval.name());
            statement.setObject(4, timestamp(fromInclusive));
            statement.setObject(5, timestamp(toExclusive));
            try (ResultSet resultSet = statement.executeQuery()) {
                List<KlineEvent> events = new ArrayList<>();
                while (resultSet.next()) {
                    events.add(toKlineEvent(resultSet));
                }
                return events;
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("failed to load kline history", exception);
        }
    }

    private Connection openConnection() throws SQLException {
        return DriverManager.getConnection(jdbcUrl, username, password);
    }

    private void requireTimeRange(Instant fromInclusive, Instant toExclusive) {
        if (fromInclusive == null || toExclusive == null) {
            throw new IllegalArgumentException("time range must not be null");
        }
        if (!fromInclusive.isBefore(toExclusive)) {
            throw new IllegalArgumentException("fromInclusive must be before toExclusive");
        }
    }

    private TradeEvent toTradeEvent(ResultSet resultSet) throws SQLException {
        return new TradeEvent(
                instrument(resultSet),
                instant(resultSet, "event_time"),
                instant(resultSet, "received_at"),
                resultSet.getString("trade_id"),
                resultSet.getObject("price", BigDecimal.class),
                resultSet.getObject("quantity", BigDecimal.class),
                TradeSide.valueOf(resultSet.getString("side"))
        );
    }

    private KlineEvent toKlineEvent(ResultSet resultSet) throws SQLException {
        return new KlineEvent(
                instrument(resultSet),
                instant(resultSet, "event_time"),
                instant(resultSet, "received_at"),
                KlineInterval.valueOf(resultSet.getString("interval_name")),
                instant(resultSet, "open_time"),
                instant(resultSet, "close_time"),
                resultSet.getObject("open_price", BigDecimal.class),
                resultSet.getObject("high_price", BigDecimal.class),
                resultSet.getObject("low_price", BigDecimal.class),
                resultSet.getObject("close_price", BigDecimal.class),
                resultSet.getObject("volume", BigDecimal.class),
                resultSet.getBoolean("closed")
        );
    }

    private Instrument instrument(ResultSet resultSet) throws SQLException {
        return new Instrument(
                Exchange.valueOf(resultSet.getString("exchange_name")),
                resultSet.getString("symbol")
        );
    }

    private Instant instant(ResultSet resultSet, String columnName) throws SQLException {
        OffsetDateTime value = resultSet.getObject(columnName, OffsetDateTime.class);
        return value.toInstant();
    }

    private OffsetDateTime timestamp(Instant instant) {
        return instant.atOffset(ZoneOffset.UTC);
    }
}
