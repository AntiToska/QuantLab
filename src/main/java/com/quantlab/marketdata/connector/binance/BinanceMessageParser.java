package com.quantlab.marketdata.connector.binance;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import org.springframework.stereotype.Component;

/**
 * Binance 消息解析器。
 * <p>
 * 先根据事件类型路由到不同 DTO，再交给映射器转换为内部事件。
 */
@Component
public class BinanceMessageParser {

    private final ObjectMapper objectMapper;

    public BinanceMessageParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * 解析原始 Binance JSON 文本，并按事件类型路由到对应 DTO。
     */
    public BinancePayload parse(String payload) {
        try {
            JsonNode root = objectMapper.readTree(payload);
            if (!root.hasNonNull("e")) {
                return BinancePayload.control();
            }
            String eventType = text(root, "e");

            return switch (eventType) {
                case "trade" -> BinancePayload.trade(objectMapper.treeToValue(root, BinanceTradeMessage.class));
                case "kline" -> BinancePayload.kline(objectMapper.treeToValue(root, BinanceKlineMessage.class));
                default -> throw new IllegalArgumentException("Unsupported Binance event type: " + eventType);
            };
        } catch (IOException exception) {
            throw new IllegalArgumentException("Failed to parse Binance payload", exception);
        }
    }

    /**
     * 安全读取必填字段，缺失时直接抛出协议错误。
     */
    private String text(JsonNode node, String fieldName) {
        JsonNode child = node.get(fieldName);
        if (child == null || child.isNull()) {
            throw new IllegalArgumentException("Missing field in Binance payload: " + fieldName);
        }
        return child.asText();
    }
}
