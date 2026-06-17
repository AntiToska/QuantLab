package com.quantlab.analytics;

import com.quantlab.backtest.BacktestResult;
import java.util.Objects;

/**
 * 回测研究产物聚合对象。
 * <p>
 * 用来把一次研究流水线的关键输出集中返回，
 * 避免后续调用方自己拼装中间结果。
 */
public record BacktestResearchArtifact(
        BacktestResult backtestResult,
        String backtestResultJson,
        BacktestResearchReport researchReport,
        String researchReportMarkdown
) {

    public BacktestResearchArtifact {
        Objects.requireNonNull(backtestResult, "backtestResult must not be null");
        if (backtestResultJson == null || backtestResultJson.isBlank()) {
            throw new IllegalArgumentException("backtestResultJson must not be blank");
        }
        Objects.requireNonNull(researchReport, "researchReport must not be null");
        if (researchReportMarkdown == null || researchReportMarkdown.isBlank()) {
            throw new IllegalArgumentException("researchReportMarkdown must not be blank");
        }
        backtestResultJson = backtestResultJson.trim();
        researchReportMarkdown = researchReportMarkdown.trim();
    }
}
