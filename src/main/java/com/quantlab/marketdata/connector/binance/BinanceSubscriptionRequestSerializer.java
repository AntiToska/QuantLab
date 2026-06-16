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

    /**
     * 将内部订阅请求转换成 Binance WebSocket 可直接发送的 JSON 文本。
     */
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

    /**
     * 对字符串做最小转义，满足当前请求体序列化需求。
     */
    private String quote(String value) {
        return "\"" + value.replace("\"", "\\\"") + "\"";
    }
}
