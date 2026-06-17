package com.quantlab.marketdata.connector.binance;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quantlab.marketdata.model.KlineEvent;
import com.quantlab.marketdata.model.MarketDataEvent;
import com.quantlab.marketdata.model.TradeEvent;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class BinanceMessageParserTests {

    private final BinanceMessageParser parser = new BinanceMessageParser(new ObjectMapper());
    private final BinanceEventMapper mapper = new BinanceEventMapper();

    @Test
    void shouldParseTradePayload() {
        String payload = """
                {
                  "e": "trade",
                  "E": 1718506800000,
                  "s": "BTCUSDT",
                  "t": 10001,
                  "p": "105000.12",
                  "q": "0.010",
                  "m": false
                }
                """;

        BinancePayload parsedPayload = parser.parse(payload);
        MarketDataEvent event = mapper.toEvent(parsedPayload, Instant.parse("2026-06-16T04:00:00Z"));

        assertThat(event).isInstanceOf(TradeEvent.class);
        TradeEvent tradeEvent = (TradeEvent) event;
        assertThat(tradeEvent.instrument().symbol()).isEqualTo("BTCUSDT");
        assertThat(tradeEvent.price().toPlainString()).isEqualTo("105000.12");
    }

    @Test
    void shouldParseKlinePayload() {
        String payload = """
                {
                  "e": "kline",
                  "E": 1718506800000,
                  "s": "ETHUSDT",
                  "k": {
                    "t": 1718506800000,
                    "T": 1718506859999,
                    "i": "1m",
                    "o": "3500.00",
                    "c": "3501.50",
                    "h": "3502.00",
                    "l": "3499.50",
                    "v": "128.4",
                    "x": true
                  }
                }
                """;

        BinancePayload parsedPayload = parser.parse(payload);
        MarketDataEvent event = mapper.toEvent(parsedPayload, Instant.parse("2026-06-16T04:00:00Z"));

        assertThat(event).isInstanceOf(KlineEvent.class);
        KlineEvent klineEvent = (KlineEvent) event;
        assertThat(klineEvent.instrument().symbol()).isEqualTo("ETHUSDT");
        assertThat(klineEvent.interval().code()).isEqualTo("1m");
        assertThat(klineEvent.closed()).isTrue();
    }

    @Test
    void shouldTreatSubscriptionAckAsControlPayload() {
        String payload = """
                {
                  "result": null,
                  "id": 1
                }
                """;

        BinancePayload parsedPayload = parser.parse(payload);

        assertThat(parsedPayload.controlMessage()).isTrue();
    }
}
