package com.quantlab.marketdata;

import com.quantlab.common.config.QuantLabProperties;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class MarketDataModule {

    private static final Logger log = LoggerFactory.getLogger(MarketDataModule.class);

    private final QuantLabProperties properties;

    public MarketDataModule(QuantLabProperties properties) {
        this.properties = properties;
    }

    @EventListener(ApplicationReadyEvent.class)
    /**
     * 在应用启动后打印 Market Data 模块的基础配置，
     * 便于快速确认当前启用了哪些交易所和默认交易对。
     */
    public void logStartupConfiguration() {
        List<String> exchanges = properties.marketData().exchanges();
        log.info(
                "QuantLab market data module initialized. exchanges={}, defaultSymbol={}",
                exchanges,
                properties.marketData().defaultSymbol()
        );
    }
}
