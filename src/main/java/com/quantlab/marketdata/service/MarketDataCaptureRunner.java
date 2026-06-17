package com.quantlab.marketdata.service;

import com.quantlab.common.config.QuantLabProperties;
import jakarta.annotation.PreDestroy;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URI;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * 本地实时行情采集验证 runner。
 * <p>
 * 只有显式开启时才会运行，目标是：
 * 1. 依赖应用正常启动后的真实 WebSocket 连接
 * 2. 采集固定时长
 * 3. 打印 PostgreSQL 中的增量摘要
 * 4. 自动关闭应用，便于本地联调和回归
 */
@Component
@Order(Ordered.LOWEST_PRECEDENCE)
@ConditionalOnProperty(prefix = "quantlab.market-data.capture-run", name = "enabled", havingValue = "true")
@ConditionalOnProperty(prefix = "quantlab.market-data.persistence", name = "enabled", havingValue = "true")
public class MarketDataCaptureRunner {

    private static final Logger log = LoggerFactory.getLogger(MarketDataCaptureRunner.class);

    private final QuantLabProperties properties;
    private final ConfigurableApplicationContext applicationContext;
    private final String jdbcUrl;
    private final String username;
    private final String password;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    public MarketDataCaptureRunner(
            QuantLabProperties properties,
            ConfigurableApplicationContext applicationContext,
            @Value("${quantlab.market-data.persistence.jdbc-url}") String jdbcUrl,
            @Value("${quantlab.market-data.persistence.username}") String username,
            @Value("${quantlab.market-data.persistence.password}") String password
    ) {
        this.properties = properties;
        this.applicationContext = applicationContext;
        this.jdbcUrl = jdbcUrl;
        this.username = username;
        this.password = password;
    }

    @EventListener(ApplicationReadyEvent.class)
    @Order(Ordered.LOWEST_PRECEDENCE)
    public void startCapture() {
        QuantLabProperties.MarketDataCaptureProperties config = properties.marketData().captureRun();
        String exchange = required(config.exchange(), "exchange");
        String symbol = required(config.symbol(), "symbol");
        String interval = required(config.interval(), "interval").trim().toUpperCase();
        long durationSeconds = requirePositive(config.durationSeconds(), "durationSeconds");
        WsEndpointProbe endpointProbe = probeEndpoint(exchange);
        CaptureSnapshot before = snapshot(exchange, symbol, interval);
        Instant startedAt = Instant.now();

        log.info(
                """
                Starting local market data capture run.
                exchange={}, symbol={}, interval={}, durationSeconds={}
                wsUrl={}, wsHost={}, wsPort={}
                dnsResolvedAddresses={}
                tcpReachable={}
                baselineTrades={}, baselineKlines={}
                """,
                exchange,
                symbol,
                interval,
                durationSeconds,
                endpointProbe.wsUrl(),
                endpointProbe.host(),
                endpointProbe.port(),
                endpointProbe.resolvedAddresses(),
                endpointProbe.tcpReachable(),
                before.tradeCount(),
                before.klineCount()
        );

        executorService.submit(() -> captureAndShutdown(exchange, symbol, interval, durationSeconds, startedAt, before));
    }

    @PreDestroy
    public void shutdownExecutor() {
        executorService.shutdownNow();
    }

    private void captureAndShutdown(
            String exchange,
            String symbol,
            String interval,
            long durationSeconds,
            Instant startedAt,
            CaptureSnapshot before
    ) {
        try {
            Thread.sleep(durationSeconds * 1000L);
            CaptureSnapshot after = snapshot(exchange, symbol, interval);
            Instant finishedAt = Instant.now();
            long capturedTrades = after.tradeCount() - before.tradeCount();
            long capturedKlines = after.klineCount() - before.klineCount();

            log.info(
                    """
                    Market data capture run finished.
                    exchange={}, symbol={}, interval={}
                    startedAt={}, finishedAt={}, durationSeconds={}
                    baselineTrades={}, finalTrades={}, capturedTrades={}
                    baselineKlines={}, finalKlines={}, capturedKlines={}
                    """,
                    exchange,
                    symbol,
                    interval,
                    startedAt,
                    finishedAt,
                    durationSeconds,
                    before.tradeCount(),
                    after.tradeCount(),
                    capturedTrades,
                    before.klineCount(),
                    after.klineCount(),
                    capturedKlines
            );

            if (capturedTrades == 0 && capturedKlines == 0) {
                log.warn(
                        """
                        Market data capture produced no new rows.
                        Please check outbound network access, Binance WebSocket reachability,
                        or whether the current capture window was too short to observe a closed kline.
                        """
                );
            }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            log.warn("Market data capture run interrupted before completion.");
        } catch (RuntimeException exception) {
            log.error("Market data capture run failed.", exception);
        } finally {
            applicationContext.close();
        }
    }

    private CaptureSnapshot snapshot(String exchange, String symbol, String interval) {
        try (Connection connection = DriverManager.getConnection(jdbcUrl, username, password)) {
            long tradeCount = countTrades(connection, exchange, symbol);
            long klineCount = countKlines(connection, exchange, symbol, interval);
            return new CaptureSnapshot(tradeCount, klineCount);
        } catch (SQLException exception) {
            throw new IllegalStateException("failed to query market data capture snapshot", exception);
        }
    }

    private WsEndpointProbe probeEndpoint(String exchange) {
        String wsUrl = properties.marketData()
                .connector(exchange)
                .map(QuantLabProperties.ExchangeConnectorProperties::wsUrl)
                .orElseThrow(() -> new IllegalArgumentException("unsupported capture exchange: " + exchange));
        URI uri = URI.create(required(wsUrl, "wsUrl"));
        String host = required(uri.getHost(), "wsHost");
        int port = uri.getPort() > 0 ? uri.getPort() : defaultPort(uri);
        String resolvedAddresses = resolveAddresses(host);
        boolean tcpReachable = tcpReachable(host, port);
        return new WsEndpointProbe(wsUrl, host, port, resolvedAddresses, tcpReachable);
    }

    private int defaultPort(URI uri) {
        return switch (uri.getScheme()) {
            case "wss" -> 443;
            case "ws" -> 80;
            default -> throw new IllegalArgumentException("unsupported ws scheme: " + uri.getScheme());
        };
    }

    private String resolveAddresses(String host) {
        try {
            InetAddress[] addresses = InetAddress.getAllByName(host);
            return Arrays.stream(addresses)
                    .map(InetAddress::getHostAddress)
                    .distinct()
                    .reduce((left, right) -> left + ", " + right)
                    .orElse("unresolved");
        } catch (IOException exception) {
            log.warn("Failed to resolve WebSocket host. host={}", host, exception);
            return "unresolved";
        }
    }

    private boolean tcpReachable(String host, int port) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), 3000);
            return true;
        } catch (IOException exception) {
            log.warn("WebSocket TCP preflight failed. host={}, port={}", host, port, exception);
            return false;
        }
    }

    private long countTrades(Connection connection, String exchange, String symbol) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                """
                select count(*)
                from market_data_trades
                where exchange_name = ?
                  and symbol = ?
                """
        )) {
            statement.setString(1, exchange);
            statement.setString(2, symbol);
            try (ResultSet resultSet = statement.executeQuery()) {
                resultSet.next();
                return resultSet.getLong(1);
            }
        }
    }

    private long countKlines(Connection connection, String exchange, String symbol, String interval) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                """
                select count(*)
                from market_data_klines
                where exchange_name = ?
                  and symbol = ?
                  and interval_name = ?
                """
        )) {
            statement.setString(1, exchange);
            statement.setString(2, symbol);
            statement.setString(3, interval);
            try (ResultSet resultSet = statement.executeQuery()) {
                resultSet.next();
                return resultSet.getLong(1);
            }
        }
    }

    private String required(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return value.trim();
    }

    private long requirePositive(long value, String name) {
        if (value <= 0) {
            throw new IllegalArgumentException(name + " must be greater than 0");
        }
        return value;
    }

    private record CaptureSnapshot(long tradeCount, long klineCount) {
    }

    private record WsEndpointProbe(
            String wsUrl,
            String host,
            int port,
            String resolvedAddresses,
            boolean tcpReachable
    ) {
    }
}
