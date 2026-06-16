package com.quantlab.marketdata.connector.binance;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;

class BinanceSubscriptionRequestSerializerTests {

    private final BinanceSubscriptionRequestSerializer serializer = new BinanceSubscriptionRequestSerializer();

    @Test
    void shouldSerializeSubscribeRequestToJson() {
        String json = serializer.serialize(
                BinanceSubscriptionRequest.subscribe(
                        List.of("btcusdt@trade", "btcusdt@kline_1m"),
                        1L
                )
        );

        assertThat(json).isEqualTo(
                "{\"method\":\"SUBSCRIBE\",\"params\":[\"btcusdt@trade\",\"btcusdt@kline_1m\"],\"id\":1}"
        );
    }
}
