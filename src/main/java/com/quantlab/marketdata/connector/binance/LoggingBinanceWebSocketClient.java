package com.quantlab.marketdata.connector.binance;

import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Binance WebSocket 客户端占位实现。
 * <p>
 * 当前先提供一个可替换的 stub，确保连接器流程完整，
 * 后续再接入真实 WebSocket 实现。
 */
@Component
public class LoggingBinanceWebSocketClient implements BinanceWebSocketClient {

    private static final Logger log = LoggerFactory.getLogger(LoggingBinanceWebSocketClient.class);

    @Override
    public BinanceWebSocketSession connect(Consumer<String> messageHandler) {
        log.info("Opening stub Binance WebSocket session.");
        return new LoggingBinanceWebSocketSession();
    }

    private static final class LoggingBinanceWebSocketSession implements BinanceWebSocketSession {

        @Override
        public void send(BinanceSubscriptionRequest request) {
            log.info("Sending Binance subscription request. method={}, params={}", request.method(), request.params());
        }

        @Override
        public void close() {
            log.info("Closing stub Binance WebSocket session.");
        }
    }
}
