package com.quantlab.marketdata.connector.binance;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;

/**
 * 默认 Binance WebSocket session 实现。
 */
public class DefaultBinanceWebSocketSession implements BinanceWebSocketSession {

    private final BinanceRawWebSocketFactory rawWebSocketFactory;
    private final BinanceScheduler scheduler;
    private final BinanceSubscriptionRequestSerializer serializer;
    private final String wsUrl;
    private final BinanceWebSocketListener upstreamListener;
    private final Duration reconnectDelay;
    private final Duration heartbeatInterval;
    private final List<BinanceSubscriptionRequest> subscriptions = new ArrayList<>();

    private BinanceRawWebSocket webSocket;
    private ScheduledFuture<?> reconnectTask;
    private ScheduledFuture<?> heartbeatTask;
    private BinanceSessionState state = BinanceSessionState.CONNECTING;
    private boolean manualClose;

    public DefaultBinanceWebSocketSession(
            String wsUrl,
            BinanceRawWebSocketFactory rawWebSocketFactory,
            BinanceScheduler scheduler,
            BinanceSubscriptionRequestSerializer serializer,
            Duration reconnectDelay,
            Duration heartbeatInterval,
            BinanceWebSocketListener upstreamListener
    ) {
        this.wsUrl = wsUrl;
        this.rawWebSocketFactory = rawWebSocketFactory;
        this.scheduler = scheduler;
        this.serializer = serializer;
        this.reconnectDelay = reconnectDelay;
        this.heartbeatInterval = heartbeatInterval;
        this.upstreamListener = upstreamListener;
        connect();
    }

    @Override
    public synchronized void send(BinanceSubscriptionRequest request) {
        subscriptions.add(request);
        if (state == BinanceSessionState.OPEN) {
            webSocket.sendText(serializer.serialize(request));
        }
    }

    @Override
    public synchronized BinanceSessionState state() {
        return state;
    }

    @Override
    public synchronized void close() {
        manualClose = true;
        cancelReconnectTask();
        cancelHeartbeatTask();
        state = BinanceSessionState.CLOSED;
        abortCurrentSocket();
    }

    private synchronized void connect() {
        state = BinanceSessionState.CONNECTING;
        try {
            webSocket = rawWebSocketFactory.connect(java.net.URI.create(wsUrl), new InternalListener());
            state = BinanceSessionState.OPEN;
            resendSubscriptions();
            scheduleHeartbeat();
        } catch (RuntimeException exception) {
            webSocket = null;
            upstreamListener.onError(exception);
            scheduleReconnect();
        }
    }

    private synchronized void resendSubscriptions() {
        for (BinanceSubscriptionRequest subscription : subscriptions) {
            webSocket.sendText(serializer.serialize(subscription));
        }
    }

    private synchronized void scheduleReconnect() {
        if (manualClose || reconnectTask != null) {
            return;
        }

        cancelHeartbeatTask();
        state = BinanceSessionState.RECONNECTING;
        reconnectTask = scheduler.schedule(() -> {
            synchronized (DefaultBinanceWebSocketSession.this) {
                reconnectTask = null;
                if (!manualClose) {
                    connect();
                }
            }
        }, reconnectDelay);
    }

    private synchronized void scheduleHeartbeat() {
        cancelHeartbeatTask();
        heartbeatTask = scheduler.scheduleAtFixedRate(() -> {
            synchronized (DefaultBinanceWebSocketSession.this) {
                if (!manualClose && state == BinanceSessionState.OPEN && webSocket != null) {
                    try {
                        webSocket.sendPing();
                    } catch (RuntimeException exception) {
                        handleTransportFailure(exception);
                    }
                }
            }
        }, heartbeatInterval, heartbeatInterval);
    }

    /**
     * 统一处理底层传输异常，避免心跳失败、IO 异常等场景走出不同恢复路径。
     */
    private synchronized void handleTransportFailure(RuntimeException exception) {
        abortCurrentSocket();
        webSocket = null;
        upstreamListener.onError(exception);
        scheduleReconnect();
    }

    private synchronized void cancelReconnectTask() {
        if (reconnectTask != null) {
            reconnectTask.cancel(false);
            reconnectTask = null;
        }
    }

    private synchronized void cancelHeartbeatTask() {
        if (heartbeatTask != null) {
            heartbeatTask.cancel(false);
            heartbeatTask = null;
        }
    }

    private synchronized void abortCurrentSocket() {
        if (webSocket != null) {
            webSocket.abort();
        }
    }

    private final class InternalListener implements BinanceWebSocketListener {

        @Override
        public void onMessage(String payload) {
            upstreamListener.onMessage(payload);
        }

        @Override
        public void onError(Throwable throwable) {
            upstreamListener.onError(throwable);
            abortCurrentSocket();
            webSocket = null;
            scheduleReconnect();
        }

        @Override
        public void onClosed() {
            upstreamListener.onClosed();
            abortCurrentSocket();
            webSocket = null;
            scheduleReconnect();
        }
    }
}
