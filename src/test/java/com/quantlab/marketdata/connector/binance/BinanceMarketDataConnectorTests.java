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
    private final BinanceMarketDataConnector connector = new BinanceMarketDataConnector(
            publisher,
            new BinanceMessageParser(new ObjectMapper()),
            new BinanceEventMapper(),
            new BinanceStreamNameBuilder()
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

    private static final class RecordingPublisher implements MarketDataEventPublisher {

        private final List<MarketDataEvent> events = new ArrayList<>();

        @Override
        public void publish(MarketDataEvent event) {
            events.add(event);
        }
    }
}
