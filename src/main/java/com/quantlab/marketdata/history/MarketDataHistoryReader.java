package com.quantlab.marketdata.history;

import com.quantlab.marketdata.model.Instrument;
import com.quantlab.marketdata.model.KlineEvent;
import com.quantlab.marketdata.model.KlineInterval;
import com.quantlab.marketdata.model.TradeEvent;
import java.time.Instant;
import java.util.List;

/**
 * 历史行情读取入口。
 * <p>
 * Backtest Core 后续只应该依赖这个方向的读取能力，
 * 不直接关心底层数据来自 PostgreSQL 还是别的存储。
 */
public interface MarketDataHistoryReader {

    List<TradeEvent> loadTrades(Instrument instrument, Instant fromInclusive, Instant toExclusive);

    List<KlineEvent> loadKlines(
            Instrument instrument,
            KlineInterval interval,
            Instant fromInclusive,
            Instant toExclusive
    );
}
