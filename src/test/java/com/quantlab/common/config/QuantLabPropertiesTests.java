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
                    "quantlab.market-data.default-symbol=ETHUSDT"
            );

    @Test
    void bindsMarketDataProperties() {
        contextRunner.run(context -> {
            QuantLabProperties properties = context.getBean(QuantLabProperties.class);

            assertThat(properties.marketData().exchanges()).containsExactly("binance", "okx");
            assertThat(properties.marketData().defaultSymbol()).isEqualTo("ETHUSDT");
        });
    }

    @Configuration(proxyBeanMethods = false)
    @EnableConfigurationProperties(QuantLabProperties.class)
    static class TestConfiguration {
    }
}
