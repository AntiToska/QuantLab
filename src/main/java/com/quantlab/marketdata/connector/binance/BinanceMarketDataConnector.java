package com.quantlab.marketdata.connector.binance;

import com.quantlab.marketdata.connector.AbstractMarketDataConnector;
import com.quantlab.marketdata.connector.MarketDataEventPublisher;
import com.quantlab.marketdata.model.Exchange;
import com.quantlab.marketdata.model.MarketDataEvent;
import java.time.Instant;
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

    private final BinanceMessageParser messageParser;
    private final BinanceEventMapper eventMapper;
    private final BinanceStreamNameBuilder streamNameBuilder;
    private final BinanceWebSocketClient webSocketClient;
    private BinanceSubscriptionRequest lastSubscriptionRequest;
    private BinanceWebSocketSession activeSession;

    public BinanceMarketDataConnector(
            MarketDataEventPublisher eventPublisher,
            BinanceMessageParser messageParser,
            BinanceEventMapper eventMapper,
            BinanceStreamNameBuilder streamNameBuilder,
            BinanceWebSocketClient webSocketClient
    ) {
        super(Exchange.BINANCE, eventPublisher);
        this.messageParser = messageParser;
        this.eventMapper = eventMapper;
        this.streamNameBuilder = streamNameBuilder;
        this.webSocketClient = webSocketClient;
    }

    @Override
    protected void doStart(List<String> symbols) {
        activeSession = webSocketClient.connect(payload -> handleMessage(payload, Instant.now()));
        lastSubscriptionRequest = BinanceSubscriptionRequest.subscribe(
                streamNameBuilder.buildDefaultStreams(symbols),
                1L
        );
        activeSession.send(lastSubscriptionRequest);
        log.info(
                "Starting Binance market data connector. symbols={}, streams={}",
                symbols,
                lastSubscriptionRequest.params()
        );
    }

    @Override
    public void stop() {
        if (activeSession != null) {
            activeSession.close();
            activeSession = null;
        }
        log.info("Stopping Binance market data connector.");
    }

    /**
     * 处理原始 Binance 消息。
     * <p>
     * 当前先暴露为显式方法，便于在没有真实 WebSocket 客户端前完成协议层测试。
     */
    public void handleMessage(String payload, Instant receivedAt) {
        BinancePayload parsedPayload = messageParser.parse(payload);
        MarketDataEvent event = eventMapper.toEvent(parsedPayload, receivedAt);
        eventPublisher.publish(event);
    }

    /**
     * 返回最近一次生成的订阅请求。
     * <p>
     * 主要用于当前阶段的测试和后续 WebSocket 客户端对接。
     */
    public BinanceSubscriptionRequest lastSubscriptionRequest() {
        return lastSubscriptionRequest;
    }
}
