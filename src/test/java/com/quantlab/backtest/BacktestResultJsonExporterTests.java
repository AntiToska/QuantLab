package com.quantlab.backtest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quantlab.common.config.JacksonConfig;
import com.quantlab.marketdata.model.Exchange;
import com.quantlab.marketdata.model.Instrument;
import com.quantlab.marketdata.model.KlineInterval;
import java.math.BigDecimal;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class BacktestResultJsonExporterTests {

    private final ObjectMapper objectMapper = new JacksonConfig().objectMapper();
    private final BacktestResultJsonExporter exporter = new BacktestResultJsonExporter(objectMapper);

    @Test
    void shouldExportStructuredBacktestResultAsJson() {
        BacktestResult result = new BacktestResult(
                "close-price-momentum",
                new Instrument(Exchange.BINANCE, "BTCUSDT"),
                KlineInterval.ONE_MINUTE,
                Instant.parse("2026-06-17T00:00:00Z"),
                Instant.parse("2026-06-17T01:00:00Z"),
                new BigDecimal("10000"),
                BigDecimal.ONE,
                60,
                20,
                20,
                20,
                new BigDecimal("10020"),
                BigDecimal.ZERO,
                new BigDecimal("10020"),
                new BacktestMetrics(
                        new BigDecimal("10000"),
                        new BigDecimal("10020"),
                        new BigDecimal("0.002"),
                        new BigDecimal("1.23"),
                        new BigDecimal("0.01"),
                        40,
                        new BigDecimal("0.55")
                )
        );

        String json = exporter.export(result);

        assertThat(json).contains("\"strategyName\" : \"close-price-momentum\"");
        assertThat(json).contains("\"exchange\" : \"BINANCE\"");
        assertThat(json).contains("\"symbol\" : \"BTCUSDT\"");
        assertThat(json).contains("\"interval\" : \"ONE_MINUTE\"");
        assertThat(json).contains("\"fromInclusive\" : \"2026-06-17T00:00:00Z\"");
        assertThat(json).contains("\"initialCash\" : 10000");
        assertThat(json).contains("\"tradeQuantity\" : 1");
        assertThat(json).contains("\"totalReturn\" : 0.002");
        assertThat(json).contains("\"sharpeRatio\" : 1.23");
        assertThat(json).contains("\"maxDrawdown\" : 0.01");
        assertThat(json).contains("\"winRate\" : 0.55");
    }

    @Test
    void shouldRejectNullResult() {
        assertThatThrownBy(() -> exporter.export(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("result");
    }
}
