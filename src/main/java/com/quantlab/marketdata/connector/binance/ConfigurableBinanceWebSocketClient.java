package com.quantlab.marketdata.connector.binance;

import com.quantlab.common.config.QuantLabProperties;
import java.time.Duration;
import java.net.URI;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

/**
 * 可配置的 Binance WebSocket 客户端实现。
 * <p>
 * 默认使用配置中的 Binance `wsUrl` 建立真实连接。
 */
@Component
@Primary
@ConditionalOnProperty(
        prefix = "quantlab.market-data.binance",
        name = "real-client-enabled",
        havingValue = "true"
)
public class ConfigurableBinanceWebSocketClient implements BinanceWebSocketClient {

    private final QuantLabProperties properties;
    private final BinanceRawWebSocketFactory rawWebSocketFactory;
    private final BinanceScheduler scheduler;
    private final BinanceSubscriptionRequestSerializer serializer;

    public ConfigurableBinanceWebSocketClient(
            QuantLabProperties properties,
            BinanceRawWebSocketFactory rawWebSocketFactory,
            BinanceScheduler scheduler,
            BinanceSubscriptionRequestSerializer serializer
    ) {
        this.properties = properties;
        this.rawWebSocketFactory = rawWebSocketFactory;
        this.scheduler = scheduler;
        this.serializer = serializer;
    }

    @Override
    public BinanceWebSocketSession connect(BinanceWebSocketListener listener) {
        QuantLabProperties.ExchangeConnectorProperties config = properties.marketData().binance();
        return new DefaultBinanceWebSocketSession(
                config.wsUrl(),
                rawWebSocketFactory,
                scheduler,
                serializer,
                Duration.ofMillis(config.reconnectDelayMillis()),
                Duration.ofSeconds(config.heartbeatIntervalSeconds()),
                listener
        );
    }
}
