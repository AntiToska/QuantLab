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

#### 11. Sharpe 指标补齐

* 在 `BacktestMetrics` 中增加 `sharpeRatio`
* 当前 Sharpe 定义为：
  * 基于相邻权益点收益率
  * 不扣无风险利率
  * 按 K 线周期做年化
* `KlineBacktestEngine` 根据 `KlineInterval` 映射年化周期
* 补充 Sharpe 的回测测试和 JSON 导出测试

#### 12. AI Research 最小输入输出协议

* 在 `analytics` 模块新增 `BacktestResearchReport`
* 新增 `BacktestResearchReportGenerator`
* 当前先用规则生成中文研究结论，目的不是替代 LLM，而是先把输入输出接口固定下来
* 新增 `BacktestResearchReportMarkdownExporter`
* 当前最小报告已支持：
  * 核心结论
  * 风险提示
  * 优化建议
* 这意味着 `BacktestResult JSON -> 研究报告对象 -> Markdown 报告` 的最小链路已经成立
* 补充 AI Research 报告生成和 markdown 导出测试

#### 13. 统一研究流水线入口

* 新增 `BacktestResearchArtifact`
* 新增 `BacktestResearchPipeline`
* 当前可以通过一次调用串起：
  * `KlineBacktestEngine`
  * `BacktestResultJsonExporter`
  * `BacktestResearchReportGenerator`
  * `BacktestResearchReportMarkdownExporter`
* 这让后续真实联调时可以直接验证：
  * 历史数据读取
  * 回测执行
  * JSON 导出
  * 中文研究报告输出
* 新增流水线测试，验证一次运行即可拿到完整研究产物

#### 14. 本地研究执行器

* 在 `QuantLabProperties` 中新增 `quantlab.research.backtest-run.*` 配置
* 新增 `BacktestResearchRunner`
* 只有显式开启 `quantlab.research.backtest-run.enabled=true` 时才会在应用启动后执行
* 当前支持：
  * 从配置构造 `BacktestRequest`
  * 选择 `close-price-momentum` 策略
  * 调用 `BacktestResearchPipeline.runAndWrite(...)`
  * 将研究产物写到指定目录
* 默认仍保持关闭，避免影响无数据库依赖的本地启动和测试

#### 15. 样例历史数据灌库器

* 在 `QuantLabProperties` 中新增 `quantlab.research.seed-data.*` 配置
* 新增 `ResearchSeedDataRunner`
* 只有显式开启 `quantlab.research.seed-data.enabled=true` 时才会执行
* 当前先只生成一段连续 `KlineEvent` 样例数据并写入 PostgreSQL
* 目的不是模拟真实市场，而是保证本地 runner 能用真实数据库路径跑通研究产物输出

#### 16. 本地研究闭环真实验证

* 修正 `ResearchSeedDataRunner` 与 `BacktestResearchRunner` 的执行顺序
* 通过 `@Order` 保证本地联调时先灌库、再回测、最后输出研究产物
* 为 `JdbcMarketDataEventPublisher` 增加按业务键幂等写入能力：
  * 业务键：`exchange + symbol + interval + open_time`
  * PostgreSQL 使用 `on conflict do update`
  * H2 测试库使用 `merge into`
* 为 `market_data_klines` 增加唯一索引，避免重复 seed 造成样本膨胀
* 修正 PostgreSQL JDBC 对 `timestamptz` 的读取兼容：
  * 查询参数统一使用 `OffsetDateTime`
  * 结果读取改为 `OffsetDateTime -> Instant`
* 新增 K 线 upsert 测试，保证重复写入同一根 K 线时只保留一条记录
* 完成真实 PostgreSQL + Spring Boot 本地闭环验证：

```bash
mvn -Dmaven.repo.local=/home/antitoska/workspace/QuantLab/.m2/repository spring-boot:run -Dspring-boot.run.arguments="--quantlab.market-data.persistence.enabled=true --quantlab.market-data.binance.enabled=false --quantlab.market-data.okx.enabled=false --quantlab.research.seed-data.enabled=true --quantlab.research.backtest-run.enabled=true --quantlab.research.backtest-run.output-directory=./output/research/20260617-172002 --quantlab.research.backtest-run.from-inclusive=2026-06-17T00:00:00Z --quantlab.research.backtest-run.to-exclusive=2026-06-17T02:00:00Z --quantlab.research.seed-data.from-inclusive=2026-06-17T00:00:00Z --quantlab.research.seed-data.bars=120"
```

* 本次真实运行验证结果：
  * 数据库 `market_data_klines` 行数保持为 `120`
  * 回测结果 `processedBars=120`
  * 输出产物已生成：
    * `output/research/20260617-172002/backtest-result.json`
    * `output/research/20260617-172002/research-report.md`
* 当前测试结果已更新为：`40 tests, 0 failures`

#### 17. 实时行情采集验证入口

* 在 `QuantLabProperties` 中新增 `quantlab.market-data.capture-run.*` 配置
* 新增 `MarketDataCaptureRunner`
* 当前可以在显式开启时：
  * 拉起真实 Binance WebSocket
  * 采集固定时长
  * 统计 PostgreSQL 中 `Trade / Kline` 的基线与增量
  * 自动停机并输出联调摘要
* 新增本地联调命令说明，方便后续重复验证真实采集链路
* 本次在当前执行环境完成过一次真实联调尝试：
  * 时间：`2026-06-17 17:40 +08:00`
  * 结果：应用正常启动、自动停机、重连逻辑生效
  * 但到 Binance WebSocket 的连接持续报 `ConnectException`
  * 最终增量为：
    * `capturedTrades=0`
    * `capturedKlines=0`
* 结论：
  * 当前代码链路和采集入口可用
  * 当前执行环境到 Binance 实时 WebSocket 仍存在网络可达性问题

#### 18. 真实 Binance 代理接入闭环打通

* 为 Binance JDK WebSocket 客户端新增显式代理配置：
  * `quantlab.market-data.binance.proxy-enabled`
  * `quantlab.market-data.binance.proxy-host`
  * `quantlab.market-data.binance.proxy-port`
* 当前无需再依赖 JVM 全局代理参数或系统环境变量是否被正确继承
* 修正 `MarketDataCaptureRunner` 与 `MarketDataService` 的启动顺序
* 保证本地采集 runner 会先输出预检与基线，再启动连接器和等待采集结果
* 修正真实 Binance WebSocket 解析兼容问题：
  * 忽略订阅确认等控制类消息
  * 忽略 `Trade / Kline` DTO 中的未知字段
* 在用户本机网络环境完成一次真实 Binance + PostgreSQL 联调成功验证：
  * 时间：`2026-06-18 01:05 +08:00`
  * `wsUrl=wss://stream.binance.com:443/ws`
  * `tcpReachable=true`
  * `capturedTrades=296`
  * `capturedKlines=1`
* 这意味着当前 Phase 1 的核心链路已经在真实行情下成立：

```text
Binance WebSocket
  -> 代理接入
  -> 统一事件解析
  -> PostgreSQL
  -> 历史读取入口
```

* 当前测试结果已更新为：`41 tests, 0 failures`

### 今日问题

* 真实 PostgreSQL JDBC 与 H2 测试库在时间类型和 upsert 语法上存在方言差异，联调时需要分别兼容
* 当前样例数据是单边上涨序列，适合验证闭环，但不适合直接代表真实策略能力
* 回测指标已可用，但胜率与 Sharpe 在样例行情下参考意义有限，后续要尽快接入更真实的历史数据样本

### 下一步

* 基于真实 Binance 落库数据，验证 `JdbcMarketDataHistoryReader -> Backtest` 的真实数据回放闭环
* 评估是否增加一个“实时采集 -> 自动回测/报告”的串联 runner
* 在真实或归档历史样本上验证策略、指标和研究报告输出是否稳定
* 继续收敛研究闭环，避免过早扩展更多交易所或更重基础设施

---

## 2026-06-18

### 今日目标

* 基于真实 Binance 落库数据完成 `HistoryReader -> Backtest -> Report` 闭环验证
* 为研究执行入口补齐“自动取最近 N 根 K 线”的能力
* 更新阶段文档，正式收口 `Phase 1 - Market Data`

### 今日完成

#### 1. 最新历史窗口自动探查

* 新增 `MarketDataWindowInspector` 历史窗口探查接口
* 新增 `JdbcMarketDataWindowInspector`，支持从 PostgreSQL 反推出最近一段可回放 K 线窗口
* 新增 `KlineHistoryWindow`，统一封装：
  * `fromInclusive`
  * `toExclusive`
  * `bars`
* 在 `quantlab.research.backtest-run.*` 中新增 `latest-bars`
* `BacktestResearchRunner` 现在支持两种运行模式：
  * 显式指定 `from-inclusive / to-exclusive`
  * 只指定 `latest-bars`，由系统自动回放最近 N 根已落库 K 线

#### 2. 真实落库数据研究闭环验证

* 使用本地 PostgreSQL 中真实采集的 Binance `BTCUSDT 1m` 数据完成一次研究回放验证
* 本次运行命令核心参数：
  * `quantlab.market-data.persistence.enabled=true`
  * `quantlab.research.backtest-run.enabled=true`
  * `quantlab.research.backtest-run.exchange=BINANCE`
  * `quantlab.research.backtest-run.symbol=BTCUSDT`
  * `quantlab.research.backtest-run.interval=ONE_MINUTE`
  * `quantlab.research.backtest-run.strategy=close-price-momentum`
  * `quantlab.research.backtest-run.latest-bars=60`
* 真实运行结果：
  * 自动解析窗口：`fromInclusive=2026-06-17T01:01:00Z`
  * 自动解析窗口：`toExclusive=2026-06-17T17:06:00.999Z`
  * `processedBars=60`
* 成功生成研究产物：
  * `output/research/20260618-phase1-closeout/backtest-result.json`
  * `output/research/20260618-phase1-closeout/research-report.md`

#### 3. Phase 1 验收结论

当前已确认以下真实链路成立：

```text
Binance WebSocket
  -> 代理接入
  -> 统一事件解析
  -> PostgreSQL
  -> JdbcMarketDataHistoryReader
  -> KlineBacktestEngine
  -> backtest-result.json
  -> research-report.md
```

这意味着 `Phase 1 - Market Data` 已经完成“可采集、可落库、可回放、可供研究消费”的阶段目标，可以正式把主要工作重心切到 `Phase 2 - Backtest Core`。

#### 4. 测试与验证

* 本地回归测试通过：

```bash
mvn -Dmaven.repo.local=/home/antitoska/workspace/QuantLab/.m2/repository test
```

* 当前测试结果：`43 tests, 0 failures`

#### 5. Review 交接材料整理

* 新增 `doc/HANDOFF_REVIEW.md`
* 将当前仓库整理为可 review 状态，不再继续扩展业务功能
* 交接文档主要覆盖：
  * 当前阶段结论
  * 建议 review 顺序
  * 主链路阅读地图
  * 本轮关键新增能力
  * review 时建议重点判断的问题
* 目的不是新增设计，而是帮助后续 review 聚焦：
  * 模块边界
  * 配置收敛
  * Phase 2 承接方式

### 今日问题

* 当前托管执行环境默认不能直接访问本地 PostgreSQL，真实数据库联调仍需要在提权或用户终端环境下完成
* 当前示例策略仍然偏演示性质，真实研究结论还不能直接代表策略有效性

### 下一步

* 先完成本轮 review，并根据 review 结果决定是否做小范围收敛修正
* 继续补强 `Phase 2 - Backtest Core` 的研究运行稳定性
* 收敛策略执行和指标输出边界，避免回测入口继续膨胀
* 为后续 `Strategy + Metrics` / `AI Research` 留出更稳定的输入协议
