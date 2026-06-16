package com.quantlab.marketdata.connector.binance;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quantlab.marketdata.connector.MarketDataEventPublisher;
import com.quantlab.marketdata.model.MarketDataEvent;
import com.quantlab.marketdata.model.TradeEvent;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class BinanceMarketDataConnectorTests {

    private final RecordingPublisher publisher = new RecordingPublisher();
    private final RecordingWebSocketClient webSocketClient = new RecordingWebSocketClient();
    private final BinanceMarketDataConnector connector = new BinanceMarketDataConnector(
            publisher,
            new BinanceMessageParser(new ObjectMapper()),
            new BinanceEventMapper(),
            new BinanceStreamNameBuilder(),
            webSocketClient
    );

    @Test
    void shouldBuildSubscriptionRequestWhenStarted() {
        connector.start(List.of("BTCUSDT", "ETHUSDT"));

        BinanceSubscriptionRequest request = connector.lastSubscriptionRequest();

        assertThat(request.method()).isEqualTo("SUBSCRIBE");
        assertThat(request.params()).containsExactly(
                "btcusdt@trade",
                "btcusdt@kline_1m",
                "ethusdt@trade",
                "ethusdt@kline_1m"
        );
        assertThat(webSocketClient.session.sentRequests).containsExactly(request);
        assertThat(connector.sessionState()).isEqualTo(BinanceSessionState.OPEN);
    }

    @Test
    void shouldPublishMappedEventWhenHandlingTradeMessage() {
        connector.handleMessage("""
                {
                  "e": "trade",
                  "E": 1718506800000,
                  "s": "BTCUSDT",
                  "t": 10001,
                  "p": "105000.12",
                  "q": "0.010",
                  "m": false
                }
                """, Instant.parse("2026-06-16T04:20:00Z"));

        assertThat(publisher.events).hasSize(1);
        assertThat(publisher.events.getFirst()).isInstanceOf(TradeEvent.class);
        TradeEvent event = (TradeEvent) publisher.events.getFirst();
        assertThat(event.instrument().symbol()).isEqualTo("BTCUSDT");
    }

    @Test
    void shouldCloseActiveSessionWhenStopped() {
        connector.start(List.of("BTCUSDT"));

        connector.stop();

        assertThat(webSocketClient.session.closed).isTrue();
        assertThat(connector.sessionState()).isEqualTo(BinanceSessionState.CLOSED);
    }

    @Test
    void shouldPublishMappedEventWhenClientListenerReceivesMessage() {
        connector.start(List.of("BTCUSDT"));

        webSocketClient.listener.onMessage("""
                {
                  "e": "trade",
                  "E": 1718506800000,
                  "s": "BTCUSDT",
                  "t": 10001,
                  "p": "105000.12",
                  "q": "0.010",
                  "m": false
                }
                """);

        assertThat(publisher.events).hasSize(1);
        assertThat(publisher.events.getFirst()).isInstanceOf(TradeEvent.class);
    }

    private static final class RecordingPublisher implements MarketDataEventPublisher {

        private final List<MarketDataEvent> events = new ArrayList<>();

        @Override
        public void publish(MarketDataEvent event) {
            events.add(event);
        }
    }

    private static final class RecordingWebSocketClient implements BinanceWebSocketClient {

        private final RecordingSession session = new RecordingSession();
        private BinanceWebSocketListener listener;

        @Override
        public BinanceWebSocketSession connect(BinanceWebSocketListener listener) {
            this.listener = listener;
            return session;
        }
    }

    private static final class RecordingSession implements BinanceWebSocketSession {

        private final List<BinanceSubscriptionRequest> sentRequests = new ArrayList<>();
        private boolean closed;

        @Override
        public void send(BinanceSubscriptionRequest request) {
            sentRequests.add(request);
        }

        @Override
        public BinanceSessionState state() {
            return closed ? BinanceSessionState.CLOSED : BinanceSessionState.OPEN;
        }

        @Override
        public void close() {
            closed = true;
        }
    }
}
