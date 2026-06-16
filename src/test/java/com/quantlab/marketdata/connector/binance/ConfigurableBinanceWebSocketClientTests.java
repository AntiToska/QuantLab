package com.quantlab.marketdata.connector.binance;

import static org.assertj.core.api.Assertions.assertThat;

import com.quantlab.common.config.QuantLabProperties;
import java.net.URI;
import java.util.List;
import org.junit.jupiter.api.Test;

class ConfigurableBinanceWebSocketClientTests {

    @Test
    void shouldConnectUsingConfiguredWsUrl() {
        RecordingRawWebSocketFactory factory = new RecordingRawWebSocketFactory();
        ConfigurableBinanceWebSocketClient client = new ConfigurableBinanceWebSocketClient(
                new QuantLabProperties(
                        new QuantLabProperties.MarketDataProperties(
                                List.of("binance"),
                                "BTCUSDT",
                                new QuantLabProperties.ExchangeConnectorProperties(
                                        true,
                                        List.of("BTCUSDT"),
                                        "wss://stream.binance.com:9443/ws",
                                        true
                                ),
                                new QuantLabProperties.ExchangeConnectorProperties(false, List.of("BTCUSDT"), null, false),
                                new QuantLabProperties.ExchangeConnectorProperties(false, List.of("BTCUSDT"), null, false)
                        )
                ),
                factory,
                new BinanceSubscriptionRequestSerializer()
        );

        BinanceWebSocketSession session = client.connect(new NoOpListener());

        assertThat(factory.uri).isEqualTo(URI.create("wss://stream.binance.com:9443/ws"));
        assertThat(session.state()).isEqualTo(BinanceSessionState.OPEN);
    }

    @Test
    void shouldSerializeAndSendRequestThroughDefaultSession() {
        RecordingRawWebSocket rawWebSocket = new RecordingRawWebSocket();
        DefaultBinanceWebSocketSession session = new DefaultBinanceWebSocketSession(
                rawWebSocket,
                new BinanceSubscriptionRequestSerializer()
        );

        session.send(BinanceSubscriptionRequest.subscribe(List.of("btcusdt@trade"), 1L));

        assertThat(rawWebSocket.payloads).containsExactly(
                "{\"method\":\"SUBSCRIBE\",\"params\":[\"btcusdt@trade\"],\"id\":1}"
        );
    }

    private static final class RecordingRawWebSocketFactory implements BinanceRawWebSocketFactory {

        private URI uri;

        @Override
        public BinanceRawWebSocket connect(URI uri, BinanceWebSocketListener listener) {
            this.uri = uri;
            return new RecordingRawWebSocket();
        }
    }

    private static final class RecordingRawWebSocket implements BinanceRawWebSocket {

        private final List<String> payloads = new java.util.ArrayList<>();
        private boolean aborted;

        @Override
        public void sendText(String payload) {
            payloads.add(payload);
        }

        @Override
        public void abort() {
            aborted = true;
        }
    }

    private static final class NoOpListener implements BinanceWebSocketListener {

        @Override
        public void onMessage(String payload) {
        }

        @Override
        public void onError(Throwable throwable) {
        }

        @Override
        public void onClosed() {
        }
    }
}
