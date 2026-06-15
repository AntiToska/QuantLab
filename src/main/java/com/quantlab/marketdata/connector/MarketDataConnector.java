package com.quantlab.marketdata.connector;

import com.quantlab.marketdata.model.Exchange;
import java.util.List;

/**
 * 行情连接器统一接口。
 * <p>
 * 每个交易所的接入实现都需要收敛到这一层，
 * 由上层服务统一调度启动、停止和状态管理。
 */
public interface MarketDataConnector {

    Exchange exchange();

    boolean supports(Exchange exchange);

    void start(List<String> symbols);

    void stop();
}
