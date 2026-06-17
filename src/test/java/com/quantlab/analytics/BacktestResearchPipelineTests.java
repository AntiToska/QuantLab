package com.quantlab.analytics;

import static org.assertj.core.api.Assertions.assertThat;

import com.quantlab.backtest.BacktestRequest;
import com.quantlab.backtest.BacktestResultJsonExporter;
import com.quantlab.backtest.KlineBacktestEngine;
import com.quantlab.common.config.JacksonConfig;
import com.quantlab.marketdata.history.MarketDataHistoryReader;
import com.quantlab.marketdata.model.Exchange;
import com.quantlab.marketdata.model.Instrument;
import com.quantlab.marketdata.model.KlineEvent;
import com.quantlab.marketdata.model.KlineInterval;
import com.quantlab.marketdata.model.TradeEvent;
import com.quantlab.strategy.ClosePriceMomentumStrategy;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;

class BacktestResearchPipelineTests {

    @Test
    void shouldProduceBacktestJsonAndMarkdownReportInOneRun() {
        BacktestResearchPipeline pipeline = new BacktestResearchPipeline(
                new KlineBacktestEngine(new FixedHistoryReader(List.of(
                        kline("2026-06-17T00:00:00Z", "100", "100"),
                        kline("2026-06-17T00:01:00Z", "100", "101"),
                        kline("2026-06-17T00:02:00Z", "101", "99")
                ))),
                new BacktestResultJsonExporter(new JacksonConfig().objectMapper()),
                new BacktestResearchReportGenerator(),
                new BacktestResearchReportMarkdownExporter()
        );

        BacktestResearchArtifact artifact = pipeline.run(
                new BacktestRequest(
                        new Instrument(Exchange.BINANCE, "BTCUSDT"),
                        KlineInterval.ONE_MINUTE,
                        Instant.parse("2026-06-17T00:00:00Z"),
                        Instant.parse("2026-06-17T00:03:00Z"),
                        new BigDecimal("10000"),
                        BigDecimal.ONE
                ),
                new ClosePriceMomentumStrategy()
        );

        assertThat(artifact.backtestResult().processedBars()).isEqualTo(3);
        assertThat(artifact.backtestResultJson()).contains("\"strategyName\" : \"close-price-momentum\"");
        assertThat(artifact.researchReport().title()).contains("close-price-momentum");
        assertThat(artifact.researchReportMarkdown()).contains("## 核心结论");
    }

    private KlineEvent kline(String openTime, String openPrice, String closePrice) {
        Instant open = Instant.parse(openTime);
        BigDecimal openValue = new BigDecimal(openPrice);
        BigDecimal closeValue = new BigDecimal(closePrice);
        BigDecimal high = openValue.max(closeValue);
        BigDecimal low = openValue.min(closeValue);
        return new KlineEvent(
                new Instrument(Exchange.BINANCE, "BTCUSDT"),
                open.plusSeconds(59),
                open.plusSeconds(60),
                KlineInterval.ONE_MINUTE,
                open,
                open.plusSeconds(59),
                openValue,
                high,
                low,
                closeValue,
                new BigDecimal("10.0"),
                true
        );
    }

    private record FixedHistoryReader(List<KlineEvent> klines) implements MarketDataHistoryReader {

        @Override
        public List<TradeEvent> loadTrades(Instrument instrument, Instant fromInclusive, Instant toExclusive) {
            return List.of();
        }

        @Override
        public List<KlineEvent> loadKlines(
                Instrument instrument,
                KlineInterval interval,
                Instant fromInclusive,
                Instant toExclusive
        ) {
            return klines;
        }
    }
}
