# QuantLab Weekly Review

> Weekly Development Summary

---

## Week Of 2026-06-15

### 本周概览

本周完成了 QuantLab 项目的初始工程搭建，并从 `Market Data` 推进到了 `Backtest Core` 的最小闭环。工作重点从最初的工程骨架、统一领域模型和 Binance 协议解析，逐步扩展到真实 WebSocket 韧性、PostgreSQL 基础落库、历史行情读取、K 线回测事件循环，以及最小资金和持仓演算。

### 本周完成

* 建立 Spring Boot 单体工程骨架
* 完成 Market Data 统一事件模型
* 完成 Connector 生命周期管理骨架
* 完成 Binance `trade / kline` 消息解析与映射
* 完成真实 WebSocket transport 的重连、订阅重放与心跳基础能力
* 完成 PostgreSQL 基础落库入口
* 完成 Market Data 历史行情读取入口
* 完成 K 线策略接口与基础信号模型
* 完成 Backtest Core 最小事件循环
* 完成 Broker Simulator 与 Portfolio 的最小模型
* 建立基础测试体系并跑通本地构建

### 本周产出

* 基础工程可编译、可测试
* `marketdata` 模块已经有稳定的内部边界
* Binance 接入已具备“订阅建模 + 原始消息解析 + 标准化事件发布”的基础能力
* 真实 WebSocket 接入已经具备基础恢复能力，可为后续联调做准备
* `TradeEvent / KlineEvent` 已具备 PostgreSQL 写入能力
* `MarketDataHistoryReader` 已能按交易标的和时间窗口读取历史 `Trade / Kline`
* `KlineBacktestEngine` 已能按历史 K 线逐根驱动策略
* 回测结果已经包含基础信号统计、最终现金、最终持仓和最终权益
* 当前回归测试结果：`31 tests, 0 failures`

### 当前闭环

当前已经打通的最小链路：

```text
Binance WebSocket
    ↓
MarketDataEvent
    ↓
PostgreSQL Persistence
    ↓
MarketDataHistoryReader
    ↓
KlineBacktestEngine
    ↓
KlineStrategy
    ↓
SimulatedBroker / Portfolio
    ↓
BacktestResult
```

这条链路还不是完整量化平台，但已经具备后续继续做 Metrics 和 AI Report 的基础输入。

### 本周文档调整

* README 改为中文面向读者文档
* 项目路线收敛为：
  * Market Data
  * Backtest Core
  * Strategy + Metrics
  * AI Research
  * Paper Trading / Risk
  * Performance Scaling
* 当前阶段数据库策略明确收敛到 `PostgreSQL`
* `Kafka / Redis / ClickHouse` 调整为未来性能扩展项
* Agent Guide 与项目对外文档的技术边界已统一
* `PROJECT_BOARD` 调整为单一阶段管理文档，统一维护当前阶段、下一阶段、路线图、Backlog 和暂缓项
* 明确 `OKX / Bybit` 暂缓，当前优先跑通单交易所研究闭环

### 当前限制

* Binance 真实 WebSocket 还需要做一次本地真实联调
* PostgreSQL 落库当前使用原生 JDBC，尚未引入连接池、批量写入或重试
* OrderBook Snapshot 暂未落库
* Backtest 当前只支持单标的、单 K 线周期、固定交易数量
* Broker Simulator 暂未模拟手续费、滑点、挂单和成交失败
* Portfolio 只支持最小现金和单资产持仓
* Metrics 还没有独立模块，当前只有最终现金、持仓和权益

### 本周问题

* WSL / 受管环境网络状态不一致，影响 Maven 依赖拉取稳定性
* 早期路线对 ClickHouse 等组件着墨过多，不适合当前个人项目阶段
* 默认应用启动需要保持无数据库依赖，因此持久化和回测引擎都需要条件装配
* Backtest Core 开始引入资金模型后，测试断言需要明确交易价格和成交顺序

### 下周重点

* 补齐回测结果结构化输出
* 增加基础 Metrics：
  * total return
  * trade count
  * final equity
* 增加一个简单示例策略，例如 MA 或 Momentum
* 评估 AI Research 的最小报告输入协议
* 在本地 PostgreSQL 和真实 Binance WebSocket 下做一次完整联调
