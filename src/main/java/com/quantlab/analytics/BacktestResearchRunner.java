package com.quantlab.analytics;

import com.quantlab.common.config.QuantLabProperties;
import com.quantlab.marketdata.model.Exchange;
import com.quantlab.marketdata.model.Instrument;
import com.quantlab.marketdata.model.KlineInterval;
import com.quantlab.backtest.BacktestRequest;
import com.quantlab.strategy.ClosePriceMomentumStrategy;
import com.quantlab.strategy.KlineStrategy;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * 本地研究执行入口。
 * <p>
 * 只有显式开启时才会运行，用于把 PostgreSQL 历史数据直接转成研究产物文件。
 */
@Component
@ConditionalOnBean(BacktestResearchPipeline.class)
@ConditionalOnProperty(prefix = "quantlab.research.backtest-run", name = "enabled", havingValue = "true")
public class BacktestResearchRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(BacktestResearchRunner.class);

    private final QuantLabProperties properties;
    private final BacktestResearchPipeline pipeline;

    public BacktestResearchRunner(QuantLabProperties properties, BacktestResearchPipeline pipeline) {
        this.properties = properties;
        this.pipeline = pipeline;
    }

    @Override
    public void run(ApplicationArguments args) {
        QuantLabProperties.BacktestRunProperties config = properties.research().backtestRun();
        BacktestResearchOutputPaths outputPaths = pipeline.runAndWrite(
                new BacktestRequest(
                        new Instrument(Exchange.fromValue(required(config.exchange(), "exchange")), required(config.symbol(), "symbol")),
                        KlineInterval.valueOf(required(config.interval(), "interval").trim().toUpperCase()),
                        Instant.parse(required(config.fromInclusive(), "fromInclusive")),
                        Instant.parse(required(config.toExclusive(), "toExclusive")),
                        new BigDecimal(required(config.initialCash(), "initialCash")),
                        new BigDecimal(required(config.tradeQuantity(), "tradeQuantity"))
                ),
                resolveStrategy(required(config.strategy(), "strategy")),
                Path.of(required(config.outputDirectory(), "outputDirectory"))
        );

        log.info(
                "Backtest research run finished. jsonPath={}, markdownPath={}",
                outputPaths.backtestResultJsonPath(),
                outputPaths.researchReportMarkdownPath()
        );
    }

    private KlineStrategy resolveStrategy(String strategy) {
        return switch (strategy.trim().toLowerCase()) {
            case "close-price-momentum" -> new ClosePriceMomentumStrategy();
            default -> throw new IllegalArgumentException("Unsupported research strategy: " + strategy);
        };
    }

    private String required(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return value.trim();
    }
}
