package com.quantlab.marketdata.connector.binance;

import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Component;

/**
 * Binance 订阅 stream 名称构造器。
 * <p>
 * 先集中管理 stream 命名规则，避免连接器内部散落字符串拼接。
 */
@Component
public class BinanceStreamNameBuilder {

    /**
     * 为每个 symbol 生成默认订阅的 stream 列表。
     * <p>
     * 当前默认订阅 trade 和 1 分钟 K 线，后续新增频道时优先扩展这里。
     */
    public List<String> buildDefaultStreams(List<String> symbols) {
        return symbols.stream()
                .map(this::normalizeSymbol)
                .flatMap(symbol -> List.of(
                        symbol + "@trade",
                        symbol + "@kline_1m"
                ).stream())
                .toList();
    }

    /**
     * Binance stream 名称要求 symbol 使用小写。
     */
    private String normalizeSymbol(String symbol) {
        return symbol.trim().toLowerCase(Locale.ROOT);
    }
}
