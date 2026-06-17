package com.quantlab.analytics;

import java.util.List;
import java.util.Objects;

/**
 * 回测研究报告。
 * <p>
 * 当前先定义最小结构，后续无论接规则引擎还是 LLM，
 * 都可以围绕这份统一输出继续演进。
 */
public record BacktestResearchReport(
        String title,
        String summary,
        List<String> findings,
        List<String> risks,
        List<String> suggestions
) {

    public BacktestResearchReport {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("title must not be blank");
        }
        if (summary == null || summary.isBlank()) {
            throw new IllegalArgumentException("summary must not be blank");
        }
        Objects.requireNonNull(findings, "findings must not be null");
        Objects.requireNonNull(risks, "risks must not be null");
        Objects.requireNonNull(suggestions, "suggestions must not be null");
        title = title.trim();
        summary = summary.trim();
        findings = List.copyOf(findings);
        risks = List.copyOf(risks);
        suggestions = List.copyOf(suggestions);
    }
}
