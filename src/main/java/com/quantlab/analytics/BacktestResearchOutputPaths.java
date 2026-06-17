package com.quantlab.analytics;

import java.nio.file.Path;
import java.util.Objects;

/**
 * 回测研究产物输出路径。
 */
public record BacktestResearchOutputPaths(
        Path backtestResultJsonPath,
        Path researchReportMarkdownPath
) {

    public BacktestResearchOutputPaths {
        Objects.requireNonNull(backtestResultJsonPath, "backtestResultJsonPath must not be null");
        Objects.requireNonNull(researchReportMarkdownPath, "researchReportMarkdownPath must not be null");
    }
}
