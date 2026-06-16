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
        @Valid MarketDataProperties marketData
) {

    /**
     * Market Data 模块配置。
     * <p>
     * 这里既定义全局默认项，也定义每个交易所自己的启用状态与订阅标的。
     */
    public record MarketDataProperties(
            @NotEmpty List<@NotBlank String> exchanges,
            @NotBlank String defaultSymbol,
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
            boolean realClientEnabled
    ) {
    }
}
