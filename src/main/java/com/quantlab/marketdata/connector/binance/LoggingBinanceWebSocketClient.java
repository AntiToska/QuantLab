package com.quantlab.marketdata.connector.binance;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Binance WebSocket 客户端占位实现。
 * <p>
 * 当前先提供一个可替换的 stub，确保连接器流程完整，
 * 后续再接入真实 WebSocket 实现。
 */
@Component
@ConditionalOnProperty(
        prefix = "quantlab.market-data.binance",
        name = "real-client-enabled",
        havingValue = "false",
        matchIfMissing = true
)
public class LoggingBinanceWebSocketClient implements BinanceWebSocketClient {

    private static final Logger log = LoggerFactory.getLogger(LoggingBinanceWebSocketClient.class);
    private final BinanceSubscriptionRequestSerializer serializer;

    public LoggingBinanceWebSocketClient(BinanceSubscriptionRequestSerializer serializer) {
        this.serializer = serializer;
    }

    @Override
    /**
     * 建立一个 stub 会话。
     * <p>
     * 当前不做真实网络连接，只通过日志验证发送与关闭流程是否正确。
     */
    public BinanceWebSocketSession connect(BinanceWebSocketListener listener) {
        log.info("Opening stub Binance WebSocket session.");
        return new LoggingBinanceWebSocketSession(serializer, listener);
    }

    private static final class LoggingBinanceWebSocketSession implements BinanceWebSocketSession {

        private final BinanceSubscriptionRequestSerializer serializer;
        private final BinanceWebSocketListener listener;
        private BinanceSessionState state = BinanceSessionState.OPEN;

        private LoggingBinanceWebSocketSession(
                BinanceSubscriptionRequestSerializer serializer,
                BinanceWebSocketListener listener
        ) {
            this.serializer = serializer;
            this.listener = listener;
        }

        @Override
        /**
         * 模拟向 Binance 发送订阅请求。
         */
        public void send(BinanceSubscriptionRequest request) {
            log.info("Sending Binance subscription request. payload={}", serializer.serialize(request));
        }

        @Override
        /**
         * 返回当前 stub 会话状态，便于连接器和测试读取。
         */
        public BinanceSessionState state() {
            return state;
        }

        @Override
        /**
         * 关闭会话并主动回调上层监听器，模拟真实连接关闭行为。
         */
        public void close() {
            state = BinanceSessionState.CLOSED;
            listener.onClosed();
            log.info("Closing stub Binance WebSocket session.");
        }
    }
}
