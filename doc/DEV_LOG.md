# QuantLab Dev Log

> Daily Development Log

---

## 2026-06-16

### 今日目标

* 初始化 QuantLab 后端工程骨架
* 建立 Market Data 模块的领域模型和接入层边界
* 推进 Binance 行情接入的协议解析基础

### 今日完成

#### 1. 工程初始化

* 创建 `Java 21 + Spring Boot 3` 项目骨架
* 补充 `pom.xml`、启动类、基础配置文件
* 建立 `common / marketdata / strategy / backtest / risk / simulator / analytics` 包结构

#### 2. Market Data 领域模型

* 定义统一行情事件接口 `MarketDataEvent`
* 新增交易所枚举 `Exchange`
* 新增交易标的值对象 `Instrument`
* 新增核心事件模型：
  * `TradeEvent`
  * `KlineEvent`
  * `OrderBookSnapshotEvent`
* 为模型补充基础合法性校验和中文注释

#### 3. Market Data 接入层骨架

* 定义 `MarketDataConnector` 统一接口
* 定义 `MarketDataEventPublisher` 发布接口
* 新增 `MarketDataService` 管理连接器启动与停止
* 新增 `BinanceMarketDataConnector` 骨架实现

#### 4. Binance 协议解析

* 新增 Binance 消息 DTO：
  * `BinanceTradeMessage`
  * `BinanceKlineMessage`
* 新增 `BinanceMessageParser`
* 新增 `BinanceEventMapper`
* 新增订阅建模：
  * `BinanceSubscriptionRequest`
  * `BinanceStreamNameBuilder`
* 打通原始消息到内部统一事件的转换链路

#### 5. 测试与构建

* 修复当前 WSL + JDK 21 环境下的测试兼容问题
* 显式提供 `ObjectMapper` Bean，解决 Spring 上下文注入失败
* 完成配置、领域模型、连接器、Binance 解析相关测试
* 本地验证通过：

```bash
mvn -Dmaven.repo.local=/home/antitoska/workspace/QuantLab/.m2/repository test
```

* 当前测试结果：`12 tests, 0 failures`

#### 6. Git 记录

* 创建任务分支：`task/market-data-infra`
* 完成并推送提交：
  * `84a2768 feat(marketdata): bootstrap market data domain and connector skeleton`
  * `29e1e4b feat(binance): add payload parsing and subscription modeling`

### 今日问题

* 受管执行环境与用户终端的网络状态一度不一致，导致 Maven 依赖解析不稳定
* 当前仓库仍缺少 `.gitignore`，本地会持续出现 `.idea/`、`.m2/`、`target/` 等未跟踪文件

### 下一步

* 为 Binance connector 增加更完整的 WebSocket 客户端抽象
* 建立订阅命令序列化与连接状态管理
* 开始为真实行情接入和后续存储落地预留接口

---

## 2026-06-17

### 今日目标

* 为真实 Binance WebSocket 补齐错误恢复、自动重连和心跳能力
* 对当前功能做一轮回归测试
* 统一 README、Agent Guide 与项目看板中的项目路线描述

### 今日完成

#### 1. 真实 WebSocket 韧性增强

* 为 Binance 真实传输层增加 `ping` 能力
* 为 WebSocket session 增加连接状态管理：
  * `CONNECTING`
  * `OPEN`
  * `RECONNECTING`
  * `CLOSED`
* 增加自动重连能力
* 增加订阅重放能力
* 增加定时心跳能力
* 补充首次建连失败与心跳失败后的恢复逻辑

#### 2. 测试与回归

* 新增真实 transport 相关测试覆盖：
  * 重连后重订阅
  * 心跳触发
  * 首次连接失败恢复
  * 心跳失败恢复
* 本地回归验证通过：

```bash
mvn -Dmaven.repo.local=/home/antitoska/workspace/QuantLab/.m2/repository test
```

* 当前测试结果：`21 tests, 0 failures`

#### 3. 文档路线收敛

* 重写 `README.md`，统一为中文面向读者文档
* 将项目路线收敛为：
  * Market Data
  * Backtest Core
  * Strategy + Metrics
  * AI Research
  * Paper Trading / Risk
  * Performance Scaling
* 明确当前阶段优先使用 `PostgreSQL`
* 将 `Kafka / Redis / ClickHouse` 调整为未来可演进技术，而不是当前技术栈承诺
* 更新 `doc/PROJECT_BOARD.md`，使待办和路线图与当前策略一致
* 更新 `doc/AGENT_GUIDE.md`，消除与 README 的技术选型冲突
* 将 `OKX / Bybit` 从当前阶段主任务中降级，避免项目演化成“交易所连接器收集器”

#### 4. Git 记录

* 完成并推送提交：
  * `b071524 feat(binance): add reconnect and heartbeat to websocket transport`

#### 5. Market Data 持久化与历史读取

* 新增基于原生 JDBC 的 `JdbcMarketDataEventPublisher`
* 默认仍保持日志发布模式，避免在未配置数据库时影响本地启动和测试
* 当显式开启 `quantlab.market-data.persistence.enabled=true` 时，可将统一行情事件写入 PostgreSQL
* 当前先支持：
  * `TradeEvent`
  * `KlineEvent`
* `OrderBookSnapshotEvent` 暂不落库，先保持跳过策略
* 新增 H2 测试覆盖，验证 `Trade / Kline` 的基础落库路径
* 当前测试结果已更新为：`24 tests, 0 failures`
* 补充 `quantlab.market-data.persistence.*` 配置说明和本地 PostgreSQL 联调文档
* 新增 `MarketDataHistoryReader` 历史行情读取接口
* 新增 `JdbcMarketDataHistoryReader`，支持按交易标的和时间窗口读取 `Trade / Kline`
* 为后续 Backtest Core 提供最小历史数据输入入口
* 当前测试结果已更新为：`27 tests, 0 failures`

#### 6. Backtest Core 最小闭环

* 新增 `KlineStrategy` 和 `StrategySignal`，建立最小策略接口
* 新增 `BacktestRequest`、`BacktestResult` 和 `KlineBacktestEngine`
* 打通从历史 K 线读取到策略逐根执行的最小回测事件循环
* 当前测试结果已更新为：`30 tests, 0 failures`
* 新增 `SimulatedBroker`、`SimulatedOrder`、`SimulatedTrade`
* 新增 `Portfolio`，支持最小现金、持仓和权益演算
* 回测结果增加 `finalCash / finalPosition / finalEquity`
* 当前测试结果已更新为：`31 tests, 0 failures`

#### 7. 当前阶段总结

当前已经形成一条最小研究闭环：

```text
Binance 行情
    -> MarketDataEvent
    -> PostgreSQL
    -> MarketDataHistoryReader
    -> KlineBacktestEngine
    -> KlineStrategy
    -> Broker / Portfolio
    -> BacktestResult
```

这条链路已经可以支撑下一步继续做结构化回测结果、基础指标和 AI Research 报告输入。

#### 8. 本地 PostgreSQL Docker 联调

* 新增 `compose.yaml`，提供本地 PostgreSQL 16 容器配置
* 默认数据库连接信息统一为：
  * database: `quantlab`
  * username: `quantlab`
  * password: `quantlab`
  * port: `5432`
* 使用 Docker Compose 启动本地数据库：

```bash
docker compose up -d postgres
```

* 容器状态验证通过：`quantlab-postgres` 已进入 `healthy`
* 使用真实 PostgreSQL 配置启动应用并验证 schema 初始化：

```bash
mvn -Dmaven.repo.local=/home/antitoska/workspace/QuantLab/.m2/repository spring-boot:run -Dspring-boot.run.arguments="--quantlab.market-data.persistence.enabled=true --quantlab.market-data.binance.enabled=false --quantlab.market-data.okx.enabled=false"
```

* 已确认本地库中创建表：
  * `market_data_trades`
  * `market_data_klines`
* 回归测试继续通过：

```bash
mvn -Dmaven.repo.local=/home/antitoska/workspace/QuantLab/.m2/repository test
```

* 当前测试结果：`31 tests, 0 failures`

#### 9. Strategy + Metrics 最小推进

* 新增 `BacktestMetrics`，为回测结果提供结构化指标字段
* 新增 `BacktestMetricsCollector`，集中计算权益曲线、最大回撤和成交统计
* `BacktestResult` 增加 `metrics` 字段，后续可直接作为 AI Research 输入
* 当前已支持的基础指标：
  * 初始权益
  * 最终权益
  * 总收益率
  * 最大回撤
  * 实际成交数
  * 胜率
* 新增 `ClosePriceMomentumStrategy` 示例策略
* 调整 `Portfolio.apply` 返回成交是否真正生效，避免现金不足或持仓不足时误计入成交指标
* 补充回测指标、空历史数据和示例策略测试
* 当前测试结果已更新为：`33 tests, 0 failures`

#### 10. 回测结果 JSON 导出闭环

* `BacktestResult` 增加回测上下文字段：
  * `instrument`
  * `interval`
  * `initialCash`
  * `tradeQuantity`
* 调整 `JacksonConfig`，补齐 Java Time 序列化支持，确保 `Instant` 以 ISO-8601 字符串输出
* 新增 `BacktestResultJsonExporter`
* 当前可以将结构化回测结果稳定导出为 JSON，作为下一阶段 AI Research 的直接输入
* 补充 JSON 导出测试，覆盖关键字段和时间格式
* 当前测试结果已更新为：`35 tests, 0 failures`

### 今日问题

* 之前的 README 与 Agent Guide 在技术选型上存在冲突，容易误导后续实现
* 早期路线过度强调 ClickHouse 等基础设施，偏离了当前最重要的学习与闭环目标
* 本地 Docker 初次联调时需要确认 WSL 当前用户是否有 Docker API 权限；必要时通过提权命令访问 Docker socket
* Sharpe Ratio 暂未实现，因为需要明确收益序列周期和年化口径，不能随便硬编码一个误导性指标
* AI Research 目前仍缺“如何消费 JSON 结果并生成中文研究报告”的最小输入输出协议

### 下一步

* 明确 Sharpe Ratio 的计算口径并补齐指标
* 评估 AI Research 最小输入输出协议
* 开启真实 Binance WebSocket 后，验证 `Trade / Kline` 能持续写入本地 PostgreSQL
