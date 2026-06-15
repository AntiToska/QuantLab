package com.quantlab.marketdata.connector.binance;

import com.quantlab.marketdata.connector.AbstractMarketDataConnector;
import com.quantlab.marketdata.connector.MarketDataEventPublisher;
import com.quantlab.marketdata.model.Exchange;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * Binance 行情连接器骨架实现。
 * <p>
 * 当前阶段先把生命周期和订阅入口立住，
 * 后续再接入真实 WebSocket 客户端。
 */
@Component
public class BinanceMarketDataConnector extends AbstractMarketDataConnector {

    public BinanceMarketDataConnector(MarketDataEventPublisher eventPublisher) {
        super(Exchange.BINANCE, eventPublisher);
    }

    @Override
    protected void doStart(List<String> symbols) {
        log.info("Starting Binance market data connector. symbols={}", symbols);
    }

    @Override
    public void stop() {
        log.info("Stopping Binance market data connector.");
    }
}
