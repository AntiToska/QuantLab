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
                    "quantlab.market-data.capture-run.enabled=true",
                    "quantlab.market-data.capture-run.exchange=BINANCE",
                    "quantlab.market-data.capture-run.symbol=BTCUSDT",
                    "quantlab.market-data.capture-run.interval=ONE_MINUTE",
                    "quantlab.market-data.capture-run.duration-seconds=45",
                    "quantlab.research.backtest-run.enabled=true",
                    "quantlab.research.backtest-run.exchange=BINANCE",
                    "quantlab.research.backtest-run.symbol=BTCUSDT",
                    "quantlab.research.backtest-run.interval=ONE_MINUTE",
                    "quantlab.research.backtest-run.from-inclusive=2026-06-17T00:00:00Z",
                    "quantlab.research.backtest-run.to-exclusive=2026-06-17T01:00:00Z",
                    "quantlab.research.backtest-run.strategy=close-price-momentum",
                    "quantlab.research.backtest-run.output-directory=./output/research",
                    "quantlab.research.backtest-run.initial-cash=10000",
                    "quantlab.research.backtest-run.trade-quantity=1",
                    "quantlab.research.seed-data.enabled=true",
                    "quantlab.research.seed-data.exchange=BINANCE",
                    "quantlab.research.seed-data.symbol=BTCUSDT",
                    "quantlab.research.seed-data.interval=ONE_MINUTE",
                    "quantlab.research.seed-data.from-inclusive=2026-06-17T00:00:00Z",
                    "quantlab.research.seed-data.bars=120",
                    "quantlab.research.seed-data.start-price=100",
                    "quantlab.research.seed-data.price-step=1",
                    "quantlab.research.seed-data.volume=10"
            );

    @Test
    void bindsMarketDataProperties() {
        contextRunner.run(context -> {
            QuantLabProperties properties = context.getBean(QuantLabProperties.class);

            assertThat(properties.marketData().exchanges()).containsExactly("binance", "okx");
            assertThat(properties.marketData().defaultSymbol()).isEqualTo("ETHUSDT");
            assertThat(properties.marketData().captureRun().enabled()).isTrue();
            assertThat(properties.marketData().captureRun().durationSeconds()).isEqualTo(45);
            assertThat(properties.research().backtestRun().enabled()).isTrue();
            assertThat(properties.research().backtestRun().strategy()).isEqualTo("close-price-momentum");
            assertThat(properties.research().backtestRun().outputDirectory()).isEqualTo("./output/research");
            assertThat(properties.research().seedData().enabled()).isTrue();
            assertThat(properties.research().seedData().bars()).isEqualTo(120);
            assertThat(properties.research().seedData().startPrice()).isEqualTo("100");
        });
    }

    @Configuration(proxyBeanMethods = false)
    @EnableConfigurationProperties(QuantLabProperties.class)
    static class TestConfiguration {
    }
}
