package com.quantlab.common.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import java.util.Optional;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "quantlab")
public record QuantLabProperties(
        @Valid MarketDataProperties marketData,
        @Valid ResearchProperties research
) {

    /**
     * Market Data 模块配置。
     * <p>
     * 这里既定义全局默认项，也定义每个交易所自己的启用状态与订阅标的。
     */
    public record MarketDataProperties(
            @NotEmpty List<@NotBlank String> exchanges,
            @NotBlank String defaultSymbol,
            @Valid MarketDataCaptureProperties captureRun,
            @Valid ExchangeConnectorProperties binance,
            @Valid ExchangeConnectorProperties okx,
            @Valid ExchangeConnectorProperties bybit
    ) {
        /**
         * 根据交易所名称返回对应连接器配置。
         */
        public Optional<ExchangeConnectorProperties> connector(String exchange) {
            return switch (exchange.trim().toLowerCase()) {
                case "binance" -> Optional.ofNullable(binance);
                case "okx" -> Optional.ofNullable(okx);
                case "bybit" -> Optional.ofNullable(bybit);
                default -> Optional.empty();
            };
        }
    }

    /**
     * 单个交易所连接器配置。
     */
    public record ExchangeConnectorProperties(
            boolean enabled,
            @NotEmpty List<@NotBlank String> symbols,
            String wsUrl,
            boolean realClientEnabled,
            boolean proxyEnabled,
            String proxyHost,
            Integer proxyPort,
            long reconnectDelayMillis,
            long heartbeatIntervalSeconds
    ) {
    }

    /**
     * 本地实时行情采集验证入口配置。
     * <p>
     * 用于在显式开启时拉起真实 WebSocket 采集固定时长，
     * 然后输出 PostgreSQL 中的增量摘要并自动停机。
     */
    public record MarketDataCaptureProperties(
            boolean enabled,
            String exchange,
            String symbol,
            String interval,
            long durationSeconds
    ) {
    }

    /**
     * Research 模块配置。
     */
    public record ResearchProperties(
            @Valid BacktestRunProperties backtestRun,
            @Valid SeedDataProperties seedData
    ) {
    }

    /**
     * 本地研究执行入口配置。
     * <p>
     * 只有显式开启时才会在应用启动后自动执行回测和报告写出。
     */
    public record BacktestRunProperties(
            boolean enabled,
            String exchange,
            String symbol,
            String interval,
            String fromInclusive,
            String toExclusive,
            String strategy,
            Integer latestBars,
            String outputDirectory,
            String initialCash,
            String tradeQuantity
    ) {
    }

    /**
     * 本地样例历史数据灌库配置。
     */
    public record SeedDataProperties(
            boolean enabled,
            String exchange,
            String symbol,
            String interval,
            String fromInclusive,
            int bars,
            String startPrice,
            String priceStep,
            String volume
    ) {
    }
}
