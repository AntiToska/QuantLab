package com.quantlab.marketdata.model;

import java.util.Objects;

/**
 * 统一交易标的值对象。
 * <p>
 * 这里先只保留交易所和符号，后续再根据合约类型、
 * 现货/永续等维度补充更细的建模。
 */
public record Instrument(Exchange exchange, String symbol) {

    public Instrument {
        Objects.requireNonNull(exchange, "exchange must not be null");
        if (symbol == null || symbol.isBlank()) {
            throw new IllegalArgumentException("symbol must not be blank");
        }

        symbol = symbol.trim().toUpperCase();
    }
}
