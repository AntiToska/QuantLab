package com.quantlab.common.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;

class QuantLabPropertiesTests {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(TestConfiguration.class)
            .withPropertyValues(
                    "quantlab.market-data.exchanges[0]=binance",
                    "quantlab.market-data.exchanges[1]=okx",
                    "quantlab.market-data.default-symbol=ETHUSDT",
                    "quantlab.research.backtest-run.enabled=true",
                    "quantlab.research.backtest-run.exchange=BINANCE",
                    "quantlab.research.backtest-run.symbol=BTCUSDT",
                    "quantlab.research.backtest-run.interval=ONE_MINUTE",
                    "quantlab.research.backtest-run.from-inclusive=2026-06-17T00:00:00Z",
                    "quantlab.research.backtest-run.to-exclusive=2026-06-17T01:00:00Z",
                    "quantlab.research.backtest-run.strategy=close-price-momentum",
                    "quantlab.research.backtest-run.output-directory=./output/research",
                    "quantlab.research.backtest-run.initial-cash=10000",
                    "quantlab.research.backtest-run.trade-quantity=1"
            );

    @Test
    void bindsMarketDataProperties() {
        contextRunner.run(context -> {
            QuantLabProperties properties = context.getBean(QuantLabProperties.class);

            assertThat(properties.marketData().exchanges()).containsExactly("binance", "okx");
            assertThat(properties.marketData().defaultSymbol()).isEqualTo("ETHUSDT");
            assertThat(properties.research().backtestRun().enabled()).isTrue();
            assertThat(properties.research().backtestRun().strategy()).isEqualTo("close-price-momentum");
            assertThat(properties.research().backtestRun().outputDirectory()).isEqualTo("./output/research");
        });
    }

    @Configuration(proxyBeanMethods = false)
    @EnableConfigurationProperties(QuantLabProperties.class)
    static class TestConfiguration {
    }
}
