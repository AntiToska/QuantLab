package com.quantlab.marketdata.connector;

import com.quantlab.marketdata.model.Exchange;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 行情连接器抽象基类。
 * <p>
 * 当前负责统一参数校验和基础日志，避免每个交易所重复样板代码。
 */
public abstract class AbstractMarketDataConnector implements MarketDataConnector {

    protected final Logger log = LoggerFactory.getLogger(getClass());

    private final Exchange exchange;
    protected final MarketDataEventPublisher eventPublisher;

    protected AbstractMarketDataConnector(Exchange exchange, MarketDataEventPublisher eventPublisher) {
        this.exchange = exchange;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public Exchange exchange() {
        return exchange;
    }

    @Override
    public boolean supports(Exchange exchange) {
        return this.exchange == exchange;
    }

    @Override
    public final void start(List<String> symbols) {
        if (symbols == null || symbols.isEmpty()) {
            throw new IllegalArgumentException("symbols must not be empty");
        }

        List<String> normalizedSymbols = symbols.stream()
                .map(String::trim)
                .map(String::toUpperCase)
                .toList();

        doStart(normalizedSymbols);
    }

    protected abstract void doStart(List<String> symbols);
}
