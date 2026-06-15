package com.quantlab.marketdata.connector;

import com.quantlab.marketdata.model.MarketDataEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 默认事件发布实现。
 * <p>
 * 在真实事件总线落地前，先用日志确认接入链路是否打通。
 */
@Component
public class LoggingMarketDataEventPublisher implements MarketDataEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(LoggingMarketDataEventPublisher.class);

    @Override
    public void publish(MarketDataEvent event) {
        log.debug(
                "Published market data event. type={}, exchange={}, symbol={}, eventTime={}",
                event.type(),
                event.instrument().exchange(),
                event.instrument().symbol(),
                event.eventTime()
        );
    }
}
