package com.quantlab.analytics;

import java.util.Objects;
import org.springframework.stereotype.Component;

/**
 * 研究报告 Markdown 导出器。
 */
@Component
public class BacktestResearchReportMarkdownExporter {

    public String export(BacktestResearchReport report) {
        Objects.requireNonNull(report, "report must not be null");

        StringBuilder builder = new StringBuilder();
        builder.append("# ").append(report.title()).append("\n\n");
        builder.append(report.summary()).append("\n\n");
        appendSection(builder, "核心结论", report.findings());
        appendSection(builder, "风险提示", report.risks());
        appendSection(builder, "优化建议", report.suggestions());
        return builder.toString().trim();
    }

    private void appendSection(StringBuilder builder, String title, java.util.List<String> lines) {
        builder.append("## ").append(title).append("\n\n");
        for (String line : lines) {
            builder.append("- ").append(line).append("\n");
        }
        builder.append("\n");
    }
}
