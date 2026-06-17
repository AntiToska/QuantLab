package com.quantlab.backtest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Objects;
import org.springframework.stereotype.Component;

/**
 * 回测结果 JSON 导出器。
 * <p>
 * 当前阶段只负责把结构化回测结果稳定导出为 JSON，
 * 供后续 AI Research 或离线归档直接消费。
 */
@Component
public class BacktestResultJsonExporter {

    private final ObjectMapper objectMapper;

    public BacktestResultJsonExporter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public String export(BacktestResult result) {
        Objects.requireNonNull(result, "result must not be null");
        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(result);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("failed to serialize backtest result", exception);
        }
    }
}
