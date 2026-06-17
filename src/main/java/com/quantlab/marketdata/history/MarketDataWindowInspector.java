package com.quantlab.marketdata.history;

import com.quantlab.marketdata.model.Instrument;
import com.quantlab.marketdata.model.KlineInterval;
import java.util.Optional;

/**
 * 历史窗口探查入口。
 * <p>
 * 用于在不手工指定时间范围时，根据已落库数据反推出可消费的最新 K 线窗口。
 */
public interface MarketDataWindowInspector {

    Optional<KlineHistoryWindow> latestKlineWindow(Instrument instrument, KlineInterval interval, int bars);
}
