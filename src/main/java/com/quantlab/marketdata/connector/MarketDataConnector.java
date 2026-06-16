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

    /**
     * 返回当前连接器对应的交易所。
     */
    Exchange exchange();

    /**
     * 判断当前连接器是否支持给定交易所。
     * <p>
     * 这个方法主要用于上层做显式判断或后续扩展更灵活的注册机制。
     */
    boolean supports(Exchange exchange);

    /**
     * 启动连接器，并为给定交易标的建立订阅。
     */
    void start(List<String> symbols);

    /**
     * 停止连接器并释放相关资源。
     */
    void stop();
}
