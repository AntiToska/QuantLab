package com.quantlab.analytics;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import org.springframework.stereotype.Component;

/**
 * 回测研究产物落盘写出器。
 * <p>
 * 当前固定输出 JSON 和 Markdown 两个文件，
 * 便于本地联调和后续人工检查。
 */
@Component
public class BacktestResearchArtifactWriter {

    public BacktestResearchOutputPaths write(Path outputDirectory, BacktestResearchArtifact artifact) {
        Objects.requireNonNull(outputDirectory, "outputDirectory must not be null");
        Objects.requireNonNull(artifact, "artifact must not be null");

        Path normalizedOutputDirectory = outputDirectory.toAbsolutePath().normalize();
        Path backtestResultJsonPath = normalizedOutputDirectory.resolve("backtest-result.json");
        Path researchReportMarkdownPath = normalizedOutputDirectory.resolve("research-report.md");
        try {
            Files.createDirectories(normalizedOutputDirectory);
            Files.writeString(backtestResultJsonPath, artifact.backtestResultJson(), StandardCharsets.UTF_8);
            Files.writeString(researchReportMarkdownPath, artifact.researchReportMarkdown(), StandardCharsets.UTF_8);
        } catch (IOException exception) {
            throw new IllegalStateException("failed to write research artifacts", exception);
        }
        return new BacktestResearchOutputPaths(backtestResultJsonPath, researchReportMarkdownPath);
    }
}
