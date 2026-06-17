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
                    "quantlab.market-data.binance.enabled=true",
                    "quantlab.market-data.binance.ws-url=wss://stream.binance.com:443/ws",
                    "quantlab.market-data.binance.real-client-enabled=true",
                    "quantlab.market-data.binance.symbols[0]=BTCUSDT",
                    "quantlab.market-data.binance.proxy-enabled=true",
                    "quantlab.market-data.binance.proxy-host=172.17.176.1",
                    "quantlab.market-data.binance.proxy-port=7890",
                    "quantlab.market-data.binance.reconnect-delay-millis=3000",
                    "quantlab.market-data.binance.heartbeat-interval-seconds=15",
                    "quantlab.market-data.okx.enabled=false",
                    "quantlab.market-data.okx.ws-url=wss://ws.okx.com:8443/ws/v5/public",
                    "quantlab.market-data.okx.real-client-enabled=false",
                    "quantlab.market-data.okx.symbols[0]=BTCUSDT",
                    "quantlab.market-data.okx.reconnect-delay-millis=3000",
                    "quantlab.market-data.okx.heartbeat-interval-seconds=15",
                    "quantlab.market-data.bybit.enabled=false",
                    "quantlab.market-data.bybit.ws-url=wss://stream.bybit.com/v5/public/spot",
                    "quantlab.market-data.bybit.real-client-enabled=false",
                    "quantlab.market-data.bybit.symbols[0]=BTCUSDT",
                    "quantlab.market-data.bybit.reconnect-delay-millis=3000",
                    "quantlab.market-data.bybit.heartbeat-interval-seconds=15",
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
            assertThat(properties.marketData().binance().proxyEnabled()).isTrue();
            assertThat(properties.marketData().binance().proxyHost()).isEqualTo("172.17.176.1");
            assertThat(properties.marketData().binance().proxyPort()).isEqualTo(7890);
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
