package com.quantlab.marketdata.model;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * 订单簿单档位数据。
 */
public record OrderBookLevel(BigDecimal price, BigDecimal quantity) {

    public OrderBookLevel {
        Objects.requireNonNull(price, "price must not be null");
        Objects.requireNonNull(quantity, "quantity must not be null");
        if (price.signum() <= 0) {
            throw new IllegalArgumentException("price must be positive");
        }
        if (quantity.signum() < 0) {
            throw new IllegalArgumentException("quantity must not be negative");
        }
    }
}
