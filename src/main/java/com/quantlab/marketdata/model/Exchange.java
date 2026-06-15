package com.quantlab.marketdata.model;

import java.util.Arrays;

/**
 * 支持的交易所枚举。
 * <p>
 * 当前阶段只保留里程碑内明确会接入的主流交易所，
 * 后续新增交易所时继续在这里集中扩展。
 */
public enum Exchange {
    BINANCE,
    OKX,
    BYBIT;

    public static Exchange fromValue(String value) {
        return Arrays.stream(values())
                .filter(exchange -> exchange.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unsupported exchange: " + value));
    }
}
