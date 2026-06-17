package com.quantlab.analytics;

import com.quantlab.backtest.BacktestRequest;
import com.quantlab.backtest.BacktestResult;
import com.quantlab.backtest.BacktestResultJsonExporter;
import com.quantlab.backtest.KlineBacktestEngine;
import com.quantlab.marketdata.history.MarketDataHistoryReader;
import com.quantlab.strategy.KlineStrategy;
import java.util.Objects;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;

/**
 * 最小回测研究流水线。
 * <p>
 * 串联回测、JSON 导出、研究报告生成和 Markdown 导出，
 * 为后续真实联调提供统一入口。
 */
@Service
@ConditionalOnBean(MarketDataHistoryReader.class)
public class BacktestResearchPipeline {

    private final KlineBacktestEngine backtestEngine;
    private final BacktestResultJsonExporter backtestResultJsonExporter;
    private final BacktestResearchReportGenerator reportGenerator;
    private final BacktestResearchReportMarkdownExporter markdownExporter;

    public BacktestResearchPipeline(
            KlineBacktestEngine backtestEngine,
            BacktestResultJsonExporter backtestResultJsonExporter,
            BacktestResearchReportGenerator reportGenerator,
            BacktestResearchReportMarkdownExporter markdownExporter
    ) {
        this.backtestEngine = backtestEngine;
        this.backtestResultJsonExporter = backtestResultJsonExporter;
        this.reportGenerator = reportGenerator;
        this.markdownExporter = markdownExporter;
    }

    public BacktestResearchArtifact run(BacktestRequest request, KlineStrategy strategy) {
        Objects.requireNonNull(request, "request must not be null");
        Objects.requireNonNull(strategy, "strategy must not be null");

        BacktestResult backtestResult = backtestEngine.run(request, strategy);
        String backtestResultJson = backtestResultJsonExporter.export(backtestResult);
        BacktestResearchReport researchReport = reportGenerator.generate(backtestResult);
        String researchReportMarkdown = markdownExporter.export(researchReport);
        return new BacktestResearchArtifact(
                backtestResult,
                backtestResultJson,
                researchReport,
                researchReportMarkdown
        );
    }
}
