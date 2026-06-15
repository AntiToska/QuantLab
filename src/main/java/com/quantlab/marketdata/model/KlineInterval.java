package com.quantlab.marketdata.model;

/**
 * K 线周期。
 */
public enum KlineInterval {
    ONE_MINUTE("1m"),
    FIVE_MINUTES("5m"),
    FIFTEEN_MINUTES("15m"),
    ONE_HOUR("1h"),
    FOUR_HOURS("4h"),
    ONE_DAY("1d");

    private final String code;

    KlineInterval(String code) {
        this.code = code;
    }

    public String code() {
        return code;
    }
}
