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

    public List<String> buildDefaultStreams(List<String> symbols) {
        return symbols.stream()
                .map(this::normalizeSymbol)
                .flatMap(symbol -> List.of(
                        symbol + "@trade",
                        symbol + "@kline_1m"
                ).stream())
                .toList();
    }

    private String normalizeSymbol(String symbol) {
        return symbol.trim().toLowerCase(Locale.ROOT);
    }
}
