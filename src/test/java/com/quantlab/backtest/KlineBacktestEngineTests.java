package com.quantlab.backtest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.quantlab.marketdata.history.MarketDataHistoryReader;
import com.quantlab.marketdata.model.Exchange;
import com.quantlab.marketdata.model.Instrument;
import com.quantlab.marketdata.model.KlineEvent;
import com.quantlab.marketdata.model.KlineInterval;
import com.quantlab.marketdata.model.TradeEvent;
import com.quantlab.strategy.ClosePriceMomentumStrategy;
import com.quantlab.strategy.KlineStrategy;
import com.quantlab.strategy.StrategySignal;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class KlineBacktestEngineTests {

    @Test
    void shouldDriveStrategyWithHistoricalKlines() {
        RecordingHistoryReader historyReader = new RecordingHistoryReader(List.of(
                kline("2026-06-17T00:00:00Z", "100", "101"),
                kline("2026-06-17T00:01:00Z", "101", "99"),
                kline("2026-06-17T00:02:00Z", "99", "99")
        ));
        KlineBacktestEngine engine = new KlineBacktestEngine(historyReader);
        RecordingStrategy strategy = new RecordingStrategy();

        BacktestResult result = engine.run(request(), strategy);

        assertThat(result.processedBars()).isEqualTo(3);
        assertThat(result.instrument()).isEqualTo(new Instrument(Exchange.BINANCE, "BTCUSDT"));
        assertThat(result.interval()).isEqualTo(KlineInterval.ONE_MINUTE);
        assertThat(result.initialCash()).isEqualByComparingTo(new BigDecimal("10000"));
        assertThat(result.tradeQuantity()).isEqualByComparingTo(BigDecimal.ONE);
        assertThat(result.buySignals()).isEqualTo(1);
        assertThat(result.sellSignals()).isEqualTo(1);
        assertThat(result.holdSignals()).isEqualTo(1);
        assertThat(result.finalCash()).isEqualByComparingTo(new BigDecimal("9998.00"));
        assertThat(result.finalPosition()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.finalEquity()).isEqualByComparingTo(new BigDecimal("9998.00"));
        assertThat(result.metrics().initialEquity()).isEqualByComparingTo(new BigDecimal("10000"));
        assertThat(result.metrics().finalEquity()).isEqualByComparingTo(new BigDecimal("9998.00"));
        assertThat(result.metrics().totalReturn()).isEqualByComparingTo(new BigDecimal("-0.0002"));
        assertThat(result.metrics().sharpeRatio()).isNegative();
        assertThat(result.metrics().maxDrawdown()).isEqualByComparingTo(new BigDecimal("0.0002"));
        assertThat(result.metrics().executedTrades()).isEqualTo(2);
        assertThat(result.metrics().winRate()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(strategy.events).hasSize(3);
        assertThat(historyReader.requestedInstrument).isEqualTo(new Instrument(Exchange.BINANCE, "BTCUSDT"));
    }

    @Test
    void shouldRejectNullStrategySignal() {
        KlineBacktestEngine engine = new KlineBacktestEngine(new RecordingHistoryReader(List.of(
                kline("2026-06-17T00:00:00Z", "100", "101")
        )));

        assertThatThrownBy(() -> engine.run(request(), new NullSignalStrategy()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("signal");
    }

    @Test
    void shouldRejectInvalidBacktestRequestRange() {
        assertThatThrownBy(() -> new BacktestRequest(
                new Instrument(Exchange.BINANCE, "BTCUSDT"),
                KlineInterval.ONE_MINUTE,
                Instant.parse("2026-06-17T00:00:00Z"),
                Instant.parse("2026-06-17T00:00:00Z"),
                new BigDecimal("10000"),
                BigDecimal.ONE
        )).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("fromInclusive");
    }

    @Test
    void shouldNotBuyWhenCashIsInsufficient() {
        KlineBacktestEngine engine = new KlineBacktestEngine(new RecordingHistoryReader(List.of(
                kline("2026-06-17T00:00:00Z", "100", "101")
        )));

        BacktestResult result = engine.run(new BacktestRequest(
                new Instrument(Exchange.BINANCE, "BTCUSDT"),
                KlineInterval.ONE_MINUTE,
                Instant.parse("2026-06-17T00:00:00Z"),
                Instant.parse("2026-06-17T00:01:00Z"),
                new BigDecimal("50"),
                BigDecimal.ONE
        ), new RecordingStrategy());

        assertThat(result.buySignals()).isEqualTo(1);
        assertThat(result.finalCash()).isEqualByComparingTo(new BigDecimal("50"));
        assertThat(result.finalPosition()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.finalEquity()).isEqualByComparingTo(new BigDecimal("50"));
        assertThat(result.metrics().executedTrades()).isZero();
        assertThat(result.metrics().sharpeRatio()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.metrics().totalReturn()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void shouldReturnEmptyMetricsWhenNoHistoricalKlinesExist() {
        KlineBacktestEngine engine = new KlineBacktestEngine(new RecordingHistoryReader(List.of()));

        BacktestResult result = engine.run(request(), new RecordingStrategy());

        assertThat(result.processedBars()).isZero();
        assertThat(result.finalEquity()).isEqualByComparingTo(new BigDecimal("10000"));
        assertThat(result.metrics().initialEquity()).isEqualByComparingTo(new BigDecimal("10000"));
        assertThat(result.metrics().finalEquity()).isEqualByComparingTo(new BigDecimal("10000"));
        assertThat(result.metrics().totalReturn()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.metrics().sharpeRatio()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.metrics().maxDrawdown()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.metrics().executedTrades()).isZero();
        assertThat(result.metrics().winRate()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void closePriceMomentumStrategyShouldEmitSignalsFromPreviousClose() {
        KlineStrategy strategy = new ClosePriceMomentumStrategy();

        assertThat(strategy.onKline(kline("2026-06-17T00:00:00Z", "100", "100")))
                .isEqualTo(StrategySignal.HOLD);
        assertThat(strategy.onKline(kline("2026-06-17T00:01:00Z", "100", "101")))
                .isEqualTo(StrategySignal.BUY);
        assertThat(strategy.onKline(kline("2026-06-17T00:02:00Z", "101", "99")))
                .isEqualTo(StrategySignal.SELL);
        assertThat(strategy.onKline(kline("2026-06-17T00:03:00Z", "99", "99")))
                .isEqualTo(StrategySignal.HOLD);
    }

    private BacktestRequest request() {
        return new BacktestRequest(
                new Instrument(Exchange.BINANCE, "BTCUSDT"),
                KlineInterval.ONE_MINUTE,
                Instant.parse("2026-06-17T00:00:00Z"),
                Instant.parse("2026-06-17T00:03:00Z"),
                new BigDecimal("10000"),
                BigDecimal.ONE
        );
    }

    private KlineEvent kline(String openTime, String openPrice, String closePrice) {
        Instant open = Instant.parse(openTime);
        BigDecimal openValue = new BigDecimal(openPrice);
        BigDecimal closeValue = new BigDecimal(closePrice);
        BigDecimal high = openValue.max(closeValue);
        BigDecimal low = openValue.min(closeValue);
        return new KlineEvent(
                new Instrument(Exchange.BINANCE, "BTCUSDT"),
                open.plusSeconds(59),
                open.plusSeconds(60),
                KlineInterval.ONE_MINUTE,
                open,
                open.plusSeconds(59),
                openValue,
                high,
                low,
                closeValue,
                new BigDecimal("10.0"),
                true
        );
    }

    private static final class RecordingHistoryReader implements MarketDataHistoryReader {

        private final List<KlineEvent> klines;
        private Instrument requestedInstrument;

        private RecordingHistoryReader(List<KlineEvent> klines) {
            this.klines = List.copyOf(klines);
        }

        @Override
        public List<TradeEvent> loadTrades(Instrument instrument, Instant fromInclusive, Instant toExclusive) {
            return List.of();
        }

        @Override
        public List<KlineEvent> loadKlines(
                Instrument instrument,
                KlineInterval interval,
                Instant fromInclusive,
                Instant toExclusive
        ) {
            this.requestedInstrument = instrument;
            return klines;
        }
    }

    private static final class RecordingStrategy implements KlineStrategy {

        private final List<KlineEvent> events = new ArrayList<>();

        @Override
        public String name() {
            return "recording";
        }

        @Override
        public StrategySignal onKline(KlineEvent event) {
            events.add(event);
            int comparison = event.closePrice().compareTo(event.openPrice());
            if (comparison > 0) {
                return StrategySignal.BUY;
            }
            if (comparison < 0) {
                return StrategySignal.SELL;
            }
            return StrategySignal.HOLD;
        }
    }

    private static final class NullSignalStrategy implements KlineStrategy {

        @Override
        public String name() {
            return "null-signal";
        }

        @Override
        public StrategySignal onKline(KlineEvent event) {
            return null;
        }
    }
}
