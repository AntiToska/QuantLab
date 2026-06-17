package com.quantlab.analytics;

import static org.assertj.core.api.Assertions.assertThat;

import com.quantlab.backtest.BacktestMetrics;
import com.quantlab.backtest.BacktestResult;
import com.quantlab.marketdata.model.Exchange;
import com.quantlab.marketdata.model.Instrument;
import com.quantlab.marketdata.model.KlineInterval;
import java.math.BigDecimal;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class BacktestResearchReportGeneratorTests {

    private final BacktestResearchReportGenerator generator = new BacktestResearchReportGenerator();
    private final BacktestResearchReportMarkdownExporter markdownExporter =
            new BacktestResearchReportMarkdownExporter();

    @Test
    void shouldGenerateChineseResearchReportFromBacktestResult() {
        BacktestResearchReport report = generator.generate(sampleResult());

        assertThat(report.title()).contains("close-price-momentum");
        assertThat(report.summary()).contains("BTCUSDT");
        assertThat(report.findings()).isNotEmpty();
        assertThat(report.risks()).isNotEmpty();
        assertThat(report.suggestions()).isNotEmpty();
        assertThat(report.findings()).anyMatch(line -> line.contains("Sharpe"));
    }

    @Test
    void shouldExportResearchReportAsMarkdown() {
        String markdown = markdownExporter.export(generator.generate(sampleResult()));

        assertThat(markdown).contains("# close-price-momentum 策略研究报告");
        assertThat(markdown).contains("## 核心结论");
        assertThat(markdown).contains("## 风险提示");
        assertThat(markdown).contains("## 优化建议");
    }

    private BacktestResult sampleResult() {
        return new BacktestResult(
                "close-price-momentum",
                new Instrument(Exchange.BINANCE, "BTCUSDT"),
                KlineInterval.ONE_MINUTE,
                Instant.parse("2026-06-17T00:00:00Z"),
                Instant.parse("2026-06-17T02:00:00Z"),
                new BigDecimal("10000"),
                BigDecimal.ONE,
                120,
                35,
                30,
                55,
                new BigDecimal("10800"),
                BigDecimal.ZERO,
                new BigDecimal("10800"),
                new BacktestMetrics(
                        new BigDecimal("10000"),
                        new BigDecimal("10800"),
                        new BigDecimal("0.08"),
                        new BigDecimal("1.20"),
                        new BigDecimal("0.06"),
                        65,
                        new BigDecimal("0.58")
                )
        );
    }
}
