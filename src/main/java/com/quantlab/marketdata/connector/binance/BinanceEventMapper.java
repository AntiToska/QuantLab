package com.quantlab.marketdata.connector.binance;

import com.quantlab.marketdata.model.Instrument;
import com.quantlab.marketdata.model.KlineEvent;
import com.quantlab.marketdata.model.KlineInterval;
import com.quantlab.marketdata.model.MarketDataEvent;
import com.quantlab.marketdata.model.TradeEvent;
import com.quantlab.marketdata.model.TradeSide;
import java.math.BigDecimal;
import java.time.Instant;
import org.springframework.stereotype.Component;

/**
 * Binance 原始消息到统一领域事件的映射器。
 */
@Component
public class BinanceEventMapper {

    public MarketDataEvent toEvent(BinancePayload payload, Instant receivedAt) {
        return switch (payload.channel()) {
            case TRADE -> toTradeEvent(payload.trade(), receivedAt);
            case KLINE -> toKlineEvent(payload.kline(), receivedAt);
        };
    }

    private TradeEvent toTradeEvent(BinanceTradeMessage trade, Instant receivedAt) {
        return new TradeEvent(
                instrument(trade.symbol()),
                Instant.ofEpochMilli(trade.eventTime()),
                receivedAt,
                String.valueOf(trade.tradeId()),
                decimal(trade.price()),
                decimal(trade.quantity()),
                trade.isBuyerMaker() ? TradeSide.SELL : TradeSide.BUY
        );
    }

    private KlineEvent toKlineEvent(BinanceKlineMessage message, Instant receivedAt) {
        BinanceKlineMessage.KlineData kline = message.kline();
        return new KlineEvent(
                instrument(message.symbol()),
                Instant.ofEpochMilli(message.eventTime()),
                receivedAt,
                interval(kline.interval()),
                Instant.ofEpochMilli(kline.openTime()),
                Instant.ofEpochMilli(kline.closeTime()),
                decimal(kline.openPrice()),
                decimal(kline.highPrice()),
                decimal(kline.lowPrice()),
                decimal(kline.closePrice()),
                decimal(kline.baseAssetVolume()),
                kline.closed()
        );
    }

    private Instrument instrument(String symbol) {
        return new Instrument(com.quantlab.marketdata.model.Exchange.BINANCE, symbol);
    }

    private BigDecimal decimal(String value) {
        return new BigDecimal(value);
    }

    private KlineInterval interval(String intervalCode) {
        return switch (intervalCode) {
            case "1m" -> KlineInterval.ONE_MINUTE;
            case "5m" -> KlineInterval.FIVE_MINUTES;
            case "15m" -> KlineInterval.FIFTEEN_MINUTES;
            case "1h" -> KlineInterval.ONE_HOUR;
            case "4h" -> KlineInterval.FOUR_HOURS;
            case "1d" -> KlineInterval.ONE_DAY;
            default -> throw new IllegalArgumentException("Unsupported Binance kline interval: " + intervalCode);
        };
    }
}
