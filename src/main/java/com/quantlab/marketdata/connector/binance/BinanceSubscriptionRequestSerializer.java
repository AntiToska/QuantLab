package com.quantlab.marketdata.connector.binance;

import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

/**
 * Binance 订阅请求序列化器。
 * <p>
 * 统一负责把内部订阅对象转换成 WebSocket 要发送的 JSON 文本。
 */
@Component
public class BinanceSubscriptionRequestSerializer {

    public String serialize(BinanceSubscriptionRequest request) {
        String params = request.params().stream()
                .map(this::quote)
                .collect(Collectors.joining(","));

        return "{\"method\":%s,\"params\":[%s],\"id\":%d}".formatted(
                quote(request.method()),
                params,
                request.id()
        );
    }

    private String quote(String value) {
        return "\"" + value.replace("\"", "\\\"") + "\"";
    }
}
