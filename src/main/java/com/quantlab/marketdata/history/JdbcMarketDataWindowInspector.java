package com.quantlab.marketdata.history;

import com.quantlab.marketdata.model.Instrument;
import com.quantlab.marketdata.model.KlineInterval;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * 基于 JDBC 的历史窗口探查实现。
 */
@Component
@ConditionalOnProperty(prefix = "quantlab.market-data.persistence", name = "enabled", havingValue = "true")
public class JdbcMarketDataWindowInspector implements MarketDataWindowInspector {

    private final String jdbcUrl;
    private final String username;
    private final String password;

    public JdbcMarketDataWindowInspector(
            @Value("${quantlab.market-data.persistence.jdbc-url}") String jdbcUrl,
            @Value("${quantlab.market-data.persistence.username}") String username,
            @Value("${quantlab.market-data.persistence.password}") String password
    ) {
        this.jdbcUrl = jdbcUrl;
        this.username = username;
        this.password = password;
    }

    @Override
    public Optional<KlineHistoryWindow> latestKlineWindow(Instrument instrument, KlineInterval interval, int bars) {
        if (bars <= 0) {
            throw new IllegalArgumentException("bars must be greater than 0");
        }

        try (Connection connection = DriverManager.getConnection(jdbcUrl, username, password);
             PreparedStatement statement = connection.prepareStatement(
                     """
                     select open_time, close_time
                     from market_data_klines
                     where exchange_name = ?
                       and symbol = ?
                       and interval_name = ?
                     order by open_time desc, id desc
                     limit ?
                     """
             )) {
            statement.setString(1, instrument.exchange().name());
            statement.setString(2, instrument.symbol());
            statement.setString(3, interval.name());
            statement.setInt(4, bars);

            try (ResultSet resultSet = statement.executeQuery()) {
                List<KlineBoundary> boundaries = new ArrayList<>();
                while (resultSet.next()) {
                    boundaries.add(new KlineBoundary(
                            resultSet.getObject("open_time", OffsetDateTime.class).toInstant(),
                            resultSet.getObject("close_time", OffsetDateTime.class).toInstant()
                    ));
                }
                if (boundaries.isEmpty()) {
                    return Optional.empty();
                }
                Collections.reverse(boundaries);
                KlineBoundary first = boundaries.getFirst();
                KlineBoundary last = boundaries.getLast();
                return Optional.of(new KlineHistoryWindow(
                        first.openTime(),
                        last.closeTime().plusSeconds(1),
                        boundaries.size()
                ));
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("failed to inspect latest kline window", exception);
        }
    }

    private record KlineBoundary(Instant openTime, Instant closeTime) {
    }
}
