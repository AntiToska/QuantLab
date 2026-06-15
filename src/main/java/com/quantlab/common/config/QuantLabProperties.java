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

    public record MarketDataProperties(
            @NotEmpty List<@NotBlank String> exchanges,
            @NotBlank String defaultSymbol,
            @Valid ExchangeConnectorProperties binance,
            @Valid ExchangeConnectorProperties okx,
            @Valid ExchangeConnectorProperties bybit
    ) {
        public Optional<ExchangeConnectorProperties> connector(String exchange) {
            return switch (exchange.trim().toLowerCase()) {
                case "binance" -> Optional.ofNullable(binance);
                case "okx" -> Optional.ofNullable(okx);
                case "bybit" -> Optional.ofNullable(bybit);
                default -> Optional.empty();
            };
        }
    }

    public record ExchangeConnectorProperties(
            boolean enabled,
            @NotEmpty List<@NotBlank String> symbols
    ) {
    }
}
