package com.quantlab.marketdata.service;

import com.quantlab.common.config.QuantLabProperties;
import com.quantlab.marketdata.connector.MarketDataConnector;
import com.quantlab.marketdata.model.Exchange;
import jakarta.annotation.PreDestroy;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

/**
 * 行情模块生命周期服务。
 * <p>
 * 负责按配置启动已启用的连接器，并统一停止资源。
 */
@Service
public class MarketDataService {

    private static final Logger log = LoggerFactory.getLogger(MarketDataService.class);

    private final QuantLabProperties properties;
    private final Map<Exchange, MarketDataConnector> connectors;

    public MarketDataService(QuantLabProperties properties, List<MarketDataConnector> connectors) {
        this.properties = properties;
        this.connectors = indexConnectors(connectors);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void startEnabledConnectors() {
        for (String exchangeName : properties.marketData().exchanges()) {
            Exchange exchange = Exchange.fromValue(exchangeName);
            QuantLabProperties.ExchangeConnectorProperties connectorProperties = properties.marketData()
                    .connector(exchangeName)
                    .orElseThrow(() -> new IllegalStateException("Missing connector config for " + exchangeName));

            if (!connectorProperties.enabled()) {
                log.info("Market data connector is disabled. exchange={}", exchange);
                continue;
            }

            MarketDataConnector connector = connectors.get(exchange);
            if (connector == null) {
                log.warn("No market data connector implementation found. exchange={}", exchange);
                continue;
            }

            connector.start(connectorProperties.symbols());
        }
    }

    @PreDestroy
    public void stopAllConnectors() {
        connectors.values().forEach(MarketDataConnector::stop);
    }

    private Map<Exchange, MarketDataConnector> indexConnectors(List<MarketDataConnector> connectors) {
        Map<Exchange, MarketDataConnector> connectorMap = new EnumMap<>(Exchange.class);
        for (MarketDataConnector connector : connectors) {
            connectorMap.put(connector.exchange(), connector);
        }
        return connectorMap;
    }
}
