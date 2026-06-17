package com.quantlab.marketdata.connector.binance;

import static org.assertj.core.api.Assertions.assertThat;

import com.quantlab.common.config.QuantLabProperties;
import java.net.URI;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.Delayed;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

class ConfigurableBinanceWebSocketClientTests {

    @Test
    void shouldConnectUsingConfiguredWsUrl() {
        RecordingRawWebSocketFactory factory = new RecordingRawWebSocketFactory(new RecordingRawWebSocket());
        RecordingScheduler scheduler = new RecordingScheduler();
        ConfigurableBinanceWebSocketClient client = new ConfigurableBinanceWebSocketClient(
                new QuantLabProperties(
                        new QuantLabProperties.MarketDataProperties(
                                List.of("binance"),
                                "BTCUSDT",
                                new QuantLabProperties.ExchangeConnectorProperties(
                                        true,
                                        List.of("BTCUSDT"),
                                        "wss://stream.binance.com:9443/ws",
                                        true,
                                        3000,
                                        15
                                ),
                                new QuantLabProperties.ExchangeConnectorProperties(false, List.of("BTCUSDT"), null, false, 3000, 15),
                                new QuantLabProperties.ExchangeConnectorProperties(false, List.of("BTCUSDT"), null, false, 3000, 15)
                        ),
                        defaultResearchProperties()
                ),
                factory,
                scheduler,
                new BinanceSubscriptionRequestSerializer()
        );

        BinanceWebSocketSession session = client.connect(new NoOpListener());

        assertThat(factory.uri).isEqualTo(URI.create("wss://stream.binance.com:9443/ws"));
        assertThat(session.state()).isEqualTo(BinanceSessionState.OPEN);
        assertThat(scheduler.fixedRateScheduled).isTrue();
    }

    @Test
    void shouldSerializeAndSendRequestThroughDefaultSession() {
        RecordingRawWebSocket rawWebSocket = new RecordingRawWebSocket();
        RecordingRawWebSocketFactory factory = new RecordingRawWebSocketFactory(rawWebSocket);
        RecordingScheduler scheduler = new RecordingScheduler();
        DefaultBinanceWebSocketSession session = new DefaultBinanceWebSocketSession(
                "wss://stream.binance.com:9443/ws",
                factory,
                scheduler,
                new BinanceSubscriptionRequestSerializer()
                ,
                Duration.ofSeconds(3),
                Duration.ofSeconds(15),
                new NoOpListener()
        );

        session.send(BinanceSubscriptionRequest.subscribe(List.of("btcusdt@trade"), 1L));

        assertThat(rawWebSocket.payloads).containsExactly(
                "{\"method\":\"SUBSCRIBE\",\"params\":[\"btcusdt@trade\"],\"id\":1}"
        );
    }

    @Test
    void shouldReconnectAndResubscribeAfterUnexpectedClose() {
        RecordingRawWebSocket firstSocket = new RecordingRawWebSocket();
        RecordingRawWebSocket secondSocket = new RecordingRawWebSocket();
        RecordingRawWebSocketFactory factory = new RecordingRawWebSocketFactory(firstSocket, secondSocket);
        RecordingScheduler scheduler = new RecordingScheduler();

        DefaultBinanceWebSocketSession session = new DefaultBinanceWebSocketSession(
                "wss://stream.binance.com:9443/ws",
                factory,
                scheduler,
                new BinanceSubscriptionRequestSerializer(),
                Duration.ofSeconds(3),
                Duration.ofSeconds(15),
                new NoOpListener()
        );
        session.send(BinanceSubscriptionRequest.subscribe(List.of("btcusdt@trade"), 1L));

        factory.lastListener.onClosed();
        scheduler.runScheduledTask();

        assertThat(session.state()).isEqualTo(BinanceSessionState.OPEN);
        assertThat(secondSocket.payloads).containsExactly(
                "{\"method\":\"SUBSCRIBE\",\"params\":[\"btcusdt@trade\"],\"id\":1}"
        );
    }

    @Test
    void shouldSendHeartbeatWhenScheduledTaskRuns() {
        RecordingRawWebSocket socket = new RecordingRawWebSocket();
        RecordingRawWebSocketFactory factory = new RecordingRawWebSocketFactory(socket);
        RecordingScheduler scheduler = new RecordingScheduler();

        new DefaultBinanceWebSocketSession(
                "wss://stream.binance.com:9443/ws",
                factory,
                scheduler,
                new BinanceSubscriptionRequestSerializer(),
                Duration.ofSeconds(3),
                Duration.ofSeconds(15),
                new NoOpListener()
        );

        scheduler.runFixedRateTask();

        assertThat(socket.pingCount).isEqualTo(1);
    }

    @Test
    void shouldReconnectWhenInitialConnectFails() {
        RecordingRawWebSocket socket = new RecordingRawWebSocket();
        RecordingRawWebSocketFactory factory = new RecordingRawWebSocketFactory(socket);
        factory.failNextConnect(new IllegalStateException("connect failed"));
        RecordingScheduler scheduler = new RecordingScheduler();
        RecordingListener listener = new RecordingListener();

        DefaultBinanceWebSocketSession session = new DefaultBinanceWebSocketSession(
                "wss://stream.binance.com:9443/ws",
                factory,
                scheduler,
                new BinanceSubscriptionRequestSerializer(),
                Duration.ofSeconds(3),
                Duration.ofSeconds(15),
                listener
        );

        assertThat(session.state()).isEqualTo(BinanceSessionState.RECONNECTING);
        assertThat(listener.errors).hasSize(1);

        scheduler.runScheduledTask();

        assertThat(session.state()).isEqualTo(BinanceSessionState.OPEN);
        assertThat(factory.connectCount).isEqualTo(2);
    }

    @Test
    void shouldReconnectWhenHeartbeatPingFails() {
        RecordingRawWebSocket firstSocket = new RecordingRawWebSocket();
        firstSocket.failPing(new IllegalStateException("ping failed"));
        RecordingRawWebSocket secondSocket = new RecordingRawWebSocket();
        RecordingRawWebSocketFactory factory = new RecordingRawWebSocketFactory(firstSocket, secondSocket);
        RecordingScheduler scheduler = new RecordingScheduler();
        RecordingListener listener = new RecordingListener();

        DefaultBinanceWebSocketSession session = new DefaultBinanceWebSocketSession(
                "wss://stream.binance.com:9443/ws",
                factory,
                scheduler,
                new BinanceSubscriptionRequestSerializer(),
                Duration.ofSeconds(3),
                Duration.ofSeconds(15),
                listener
        );

        scheduler.runFixedRateTask();

        assertThat(session.state()).isEqualTo(BinanceSessionState.RECONNECTING);
        assertThat(firstSocket.aborted).isTrue();
        assertThat(listener.errors).hasSize(1);

        scheduler.runScheduledTask();

        assertThat(session.state()).isEqualTo(BinanceSessionState.OPEN);
        assertThat(factory.connectCount).isEqualTo(2);
    }

    private static final class RecordingRawWebSocketFactory implements BinanceRawWebSocketFactory {

        private URI uri;
        private final java.util.ArrayDeque<RecordingRawWebSocket> sockets = new java.util.ArrayDeque<>();
        private BinanceWebSocketListener lastListener;
        private RuntimeException nextConnectFailure;
        private int connectCount;

        private RecordingRawWebSocketFactory(RecordingRawWebSocket... sockets) {
            this.sockets.addAll(List.of(sockets));
        }

        private void failNextConnect(RuntimeException exception) {
            this.nextConnectFailure = exception;
        }

        @Override
        public BinanceRawWebSocket connect(URI uri, BinanceWebSocketListener listener) {
            connectCount++;
            this.uri = uri;
            this.lastListener = listener;
            if (nextConnectFailure != null) {
                RuntimeException exception = nextConnectFailure;
                nextConnectFailure = null;
                throw exception;
            }
            return sockets.removeFirst();
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

    private static final class RecordingRawWebSocket implements BinanceRawWebSocket {

        private final List<String> payloads = new java.util.ArrayList<>();
        private boolean aborted;
        private int pingCount;
        private RuntimeException pingFailure;

        private void failPing(RuntimeException exception) {
            this.pingFailure = exception;
        }

        @Override
        public void sendText(String payload) {
            payloads.add(payload);
        }

        @Override
        public void sendPing() {
            if (pingFailure != null) {
                throw pingFailure;
            }
            pingCount++;
        }

        @Override
        public void abort() {
            aborted = true;
        }
    }

    private static final class RecordingListener implements BinanceWebSocketListener {

        private final List<Throwable> errors = new java.util.ArrayList<>();

        @Override
        public void onMessage(String payload) {
        }

        @Override
        public void onError(Throwable throwable) {
            errors.add(throwable);
        }

        @Override
        public void onClosed() {
        }
    }

    private static final class RecordingScheduler implements BinanceScheduler {

        private Runnable scheduledTask;
        private Runnable fixedRateTask;
        private boolean fixedRateScheduled;

        @Override
        public ScheduledFuture<?> schedule(Runnable task, Duration delay) {
            this.scheduledTask = task;
            return new NoOpScheduledFuture();
        }

        @Override
        public ScheduledFuture<?> scheduleAtFixedRate(Runnable task, Duration initialDelay, Duration period) {
            this.fixedRateTask = task;
            this.fixedRateScheduled = true;
            return new NoOpScheduledFuture();
        }

        private void runScheduledTask() {
            if (scheduledTask != null) {
                scheduledTask.run();
            }
        }

        private void runFixedRateTask() {
            if (fixedRateTask != null) {
                fixedRateTask.run();
            }
        }
    }

    private static final class NoOpScheduledFuture implements ScheduledFuture<Object> {

        @Override
        public long getDelay(TimeUnit unit) {
            return 0;
        }

        @Override
        public int compareTo(Delayed other) {
            return 0;
        }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            return true;
        }

        @Override
        public boolean isCancelled() {
            return false;
        }

        @Override
        public boolean isDone() {
            return false;
        }

        @Override
        public Object get() {
            return null;
        }

        @Override
        public Object get(long timeout, TimeUnit unit) {
            return null;
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
