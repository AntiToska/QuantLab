package com.quantlab.analytics;

import com.quantlab.backtest.BacktestMetrics;
import com.quantlab.backtest.BacktestResult;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Component;

/**
 * 基于规则的最小研究报告生成器。
 * <p>
 * 当前先用稳定、可测试的规则生成中文结论，
 * 后续再把相同输入替换给 LLM，不改变外围接口。
 */
@Component
public class BacktestResearchReportGenerator {

    public BacktestResearchReport generate(BacktestResult result) {
        Objects.requireNonNull(result, "result must not be null");

        BacktestMetrics metrics = result.metrics();
        List<String> findings = new ArrayList<>();
        List<String> risks = new ArrayList<>();
        List<String> suggestions = new ArrayList<>();

        findings.add(returnFinding(metrics.totalReturn()));
        findings.add(sharpeFinding(metrics.sharpeRatio()));
        findings.add(winRateFinding(metrics.winRate()));

        if (metrics.maxDrawdown().compareTo(new BigDecimal("0.10")) > 0) {
            risks.add("最大回撤偏高，资金曲线存在较明显回撤压力。");
            suggestions.add("优先增加止损、仓位约束或回撤控制规则。");
        } else {
            findings.add("最大回撤仍在可控范围内。");
        }

        if (metrics.executedTrades() < 5) {
            risks.add("样本成交次数偏少，当前统计显著性不足。");
            suggestions.add("延长回测区间或扩大样本数据后再评估策略稳定性。");
        }

        if (metrics.sharpeRatio().compareTo(BigDecimal.ZERO) <= 0) {
            suggestions.add("优先优化入场过滤条件，减少低质量信号。");
        }
        if (metrics.winRate().compareTo(new BigDecimal("0.45")) < 0) {
            suggestions.add("检查出场逻辑与盈亏比设计，避免低胜率和弱收益同时出现。");
        }
        if (suggestions.isEmpty()) {
            suggestions.add("建议继续扩大样本区间，并验证不同市场状态下的稳健性。");
        }
        if (risks.isEmpty()) {
            risks.add("当前未发现特别突出的结构性风险，但仍需扩大样本验证稳健性。");
        }

        return new BacktestResearchReport(
                reportTitle(result),
                reportSummary(result),
                findings,
                risks,
                suggestions
        );
    }

    private String reportTitle(BacktestResult result) {
        return "%s 策略研究报告".formatted(result.strategyName());
    }

    private String reportSummary(BacktestResult result) {
        return "%s %s 在 %s 到 %s 区间共处理 %d 根 K 线，最终权益 %s。"
                .formatted(
                        result.instrument().symbol(),
                        result.interval().code(),
                        result.fromInclusive(),
                        result.toExclusive(),
                        result.processedBars(),
                        result.finalEquity().toPlainString()
                );
    }

    private String returnFinding(BigDecimal totalReturn) {
        if (totalReturn.compareTo(new BigDecimal("0.05")) >= 0) {
            return "策略在当前区间取得了较明显的正收益。";
        }
        if (totalReturn.compareTo(BigDecimal.ZERO) > 0) {
            return "策略收益为正，但绝对收益水平仍然有限。";
        }
        if (totalReturn.compareTo(BigDecimal.ZERO) == 0) {
            return "策略整体收益接近持平。";
        }
        return "策略在当前区间出现亏损，收益表现偏弱。";
    }

    private String sharpeFinding(BigDecimal sharpeRatio) {
        if (sharpeRatio.compareTo(new BigDecimal("1.0")) >= 0) {
            return "Sharpe 表现较好，单位波动获取收益的能力较强。";
        }
        if (sharpeRatio.compareTo(BigDecimal.ZERO) > 0) {
            return "Sharpe 为正，但风险调整后收益还有提升空间。";
        }
        return "Sharpe 不理想，当前收益对波动的补偿不足。";
    }

    private String winRateFinding(BigDecimal winRate) {
        if (winRate.compareTo(new BigDecimal("0.55")) >= 0) {
            return "策略胜率较高，信号质量整体尚可。";
        }
        if (winRate.compareTo(new BigDecimal("0.45")) >= 0) {
            return "策略胜率中性，需要结合盈亏比继续判断。";
        }
        return "策略胜率偏低，需要关注信号筛选和出场效率。";
    }
}
