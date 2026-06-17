package com.quantlab.marketdata.connector.binance;

import com.quantlab.common.config.QuantLabProperties;
import java.net.InetSocketAddress;
import java.net.ProxySelector;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.nio.ByteBuffer;
import java.util.concurrent.CompletionStage;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

/**
 * 基于 JDK HttpClient 的 Binance WebSocket 创建工厂。
 */
@Component
@Primary
public class JdkBinanceRawWebSocketFactory implements BinanceRawWebSocketFactory {

    private final HttpClient httpClient;

    public JdkBinanceRawWebSocketFactory(QuantLabProperties properties) {
        this.httpClient = buildHttpClient(properties.marketData().binance());
    }

    @Override
    public BinanceRawWebSocket connect(URI uri, BinanceWebSocketListener listener) {
        WebSocket webSocket = httpClient.newWebSocketBuilder()
                .buildAsync(uri, new ListenerAdapter(listener))
                .join();

        return new BinanceRawWebSocket() {
            @Override
            public void sendText(String payload) {
                webSocket.sendText(payload, true).join();
            }

            @Override
            public void sendPing() {
                webSocket.sendPing(ByteBuffer.wrap(new byte[]{1})).join();
            }

            @Override
            public void abort() {
                webSocket.abort();
            }
        };
    }

    private static final class ListenerAdapter implements WebSocket.Listener {

        private final BinanceWebSocketListener listener;
        private final StringBuilder textBuffer = new StringBuilder();

        private ListenerAdapter(BinanceWebSocketListener listener) {
            this.listener = listener;
        }

        @Override
        public void onOpen(WebSocket webSocket) {
            WebSocket.Listener.super.onOpen(webSocket);
            webSocket.request(1);
        }

        @Override
        public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
            textBuffer.append(data);
            if (last) {
                listener.onMessage(textBuffer.toString());
                textBuffer.setLength(0);
            }
            webSocket.request(1);
            return null;
        }

        @Override
        public CompletionStage<?> onBinary(WebSocket webSocket, ByteBuffer data, boolean last) {
            webSocket.request(1);
            return null;
        }

        @Override
        public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
            listener.onClosed();
            return WebSocket.Listener.super.onClose(webSocket, statusCode, reason);
        }

        @Override
        public void onError(WebSocket webSocket, Throwable error) {
            listener.onError(error);
        }
    }

    private HttpClient buildHttpClient(QuantLabProperties.ExchangeConnectorProperties properties) {
        HttpClient.Builder builder = HttpClient.newBuilder();
        if (properties.proxyEnabled()) {
            builder.proxy(ProxySelector.of(new InetSocketAddress(
                    required(properties.proxyHost(), "binance.proxyHost"),
                    required(properties.proxyPort(), "binance.proxyPort")
            )));
        }
        return builder.build();
    }

    private String required(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank when proxy is enabled");
        }
        return value.trim();
    }

    private int required(Integer value, String name) {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException(name + " must be greater than 0 when proxy is enabled");
        }
        return value;
    }
}
