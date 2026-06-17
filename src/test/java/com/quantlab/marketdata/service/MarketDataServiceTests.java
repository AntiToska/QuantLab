package com.quantlab.marketdata.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.quantlab.common.config.QuantLabProperties;
import com.quantlab.marketdata.connector.MarketDataConnector;
import com.quantlab.marketdata.model.Exchange;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class MarketDataServiceTests {

    @Test
    void shouldStartOnlyEnabledConnector() {
        RecordingConnector binanceConnector = new RecordingConnector(Exchange.BINANCE);
        RecordingConnector okxConnector = new RecordingConnector(Exchange.OKX);

        MarketDataService service = new MarketDataService(
                new QuantLabProperties(
                        new QuantLabProperties.MarketDataProperties(
                                List.of("binance", "okx"),
                                "BTCUSDT",
                                new QuantLabProperties.ExchangeConnectorProperties(
                                        true,
                                        List.of("BTCUSDT", "ETHUSDT"),
                                        "wss://stream.binance.com:9443/ws",
                                        false,
                                        3000,
                                        15
                                ),
                                new QuantLabProperties.ExchangeConnectorProperties(
                                        false,
                                        List.of("BTCUSDT"),
                                        "wss://ws.okx.com:8443/ws/v5/public",
                                        false,
                                        3000,
                                        15
                                ),
                                new QuantLabProperties.ExchangeConnectorProperties(
                                        false,
                                        List.of("BTCUSDT"),
                                        "wss://stream.bybit.com/v5/public/spot",
                                        false,
                                        3000,
                                        15
                                )
                        ),
                        defaultResearchProperties()
                ),
                List.of(binanceConnector, okxConnector)
        );

        service.startEnabledConnectors();

        assertThat(binanceConnector.startedSymbols).containsExactly("BTCUSDT", "ETHUSDT");
        assertThat(okxConnector.startedSymbols).isEmpty();
    }

    @Test
    void shouldStopAllConnectors() {
        RecordingConnector binanceConnector = new RecordingConnector(Exchange.BINANCE);
        RecordingConnector okxConnector = new RecordingConnector(Exchange.OKX);

        MarketDataService service = new MarketDataService(
                new QuantLabProperties(
                        new QuantLabProperties.MarketDataProperties(
                                List.of("binance"),
                                "BTCUSDT",
                                new QuantLabProperties.ExchangeConnectorProperties(
                                        true,
                                        List.of("BTCUSDT"),
                                        "wss://stream.binance.com:9443/ws",
                                        false,
                                        3000,
                                        15
                                ),
                                new QuantLabProperties.ExchangeConnectorProperties(
                                        false,
                                        List.of("BTCUSDT"),
                                        "wss://ws.okx.com:8443/ws/v5/public",
                                        false,
                                        3000,
                                        15
                                ),
                                new QuantLabProperties.ExchangeConnectorProperties(
                                        false,
                                        List.of("BTCUSDT"),
                                        "wss://stream.bybit.com/v5/public/spot",
                                        false,
                                        3000,
                                        15
                                )
                        ),
                        defaultResearchProperties()
                ),
                List.of(binanceConnector, okxConnector)
        );

        service.stopAllConnectors();

        assertThat(binanceConnector.stopped).isTrue();
        assertThat(okxConnector.stopped).isTrue();
    }

    private static final class RecordingConnector implements MarketDataConnector {

        private final Exchange exchange;
        private final List<String> startedSymbols = new ArrayList<>();
        private boolean stopped;

        private RecordingConnector(Exchange exchange) {
            this.exchange = exchange;
        }

        @Override
        public Exchange exchange() {
            return exchange;
        }

        @Override
        public boolean supports(Exchange exchange) {
            return this.exchange == exchange;
        }

        @Override
        public void start(List<String> symbols) {
            startedSymbols.addAll(symbols);
        }

        @Override
        public void stop() {
            stopped = true;
        }
    }

    private QuantLabProperties.ResearchProperties defaultResearchProperties() {
        return new QuantLabProperties.ResearchProperties(
                new QuantLabProperties.BacktestRunProperties(
                        false,
                        "BINANCE",
                        "BTCUSDT",
                        "ONE_MINUTE",
                        "2026-06-17T00:00:00Z",
                        "2026-06-17T01:00:00Z",
                        "close-price-momentum",
                        "./output/research",
                        "10000",
                        "1"
                ),
                new QuantLabProperties.SeedDataProperties(
                        false,
                        "BINANCE",
                        "BTCUSDT",
                        "ONE_MINUTE",
                        "2026-06-17T00:00:00Z",
                        120,
                        "100",
                        "1",
                        "10"
                )
        );
    }
}
