# QuantLab 交接文档

> 用于本轮代码 Review 和阶段交接

---

## 1. 当前阶段结论

当前已经完成 `Phase 1 - Market Data` 的闭环验收。

已确认的真实链路：

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

本轮 Review 的目标不是继续扩功能，而是确认这条链路是否：

* 边界清楚
* 代码职责清楚
* 配置项收敛
* 能自然承接 `Phase 2 - Backtest Core`

---

## 2. 建议 Review 顺序

建议按“先文档、再链路、后细节”的顺序看。

### 2.1 先看文档

建议先读：

* `README.md`
* `doc/PROJECT_BOARD.md`
* `doc/DEV_LOG.md`

重点确认：

* 项目路线是否与你当前目标一致
* 当前阶段是否已经从“多交易所扩展”收敛回“研究闭环”
* `Phase 1` 和 `Phase 2` 的边界是否清楚

### 2.2 再按主链路看代码

推荐顺序：

1. 实时接入
2. 消息解析
3. 落库
4. 历史读取与窗口探查
5. 回测执行
6. 研究产物输出

---

## 3. 主链路阅读地图

### 3.1 实时接入

核心文件：

* `src/main/java/com/quantlab/marketdata/connector/binance/BinanceMarketDataConnector.java`
* `src/main/java/com/quantlab/marketdata/connector/binance/ConfigurableBinanceWebSocketClient.java`
* `src/main/java/com/quantlab/marketdata/connector/binance/DefaultBinanceWebSocketSession.java`
* `src/main/java/com/quantlab/marketdata/connector/binance/JdkBinanceRawWebSocketFactory.java`

这一层负责：

* 组装 Binance 订阅流
* 建立真实或桩 WebSocket 会话
* 处理重连、重订阅、心跳
* 把原始文本消息送到解析层

Review 重点：

* 连接管理职责是否主要留在 session / client 层
* connector 是否只做“交易所接入编排”，没有塞过多连接细节
* 代理配置是否足够直接，避免依赖隐式环境变量

### 3.2 消息解析

核心文件：

* `src/main/java/com/quantlab/marketdata/connector/binance/BinanceMessageParser.java`
* `src/main/java/com/quantlab/marketdata/connector/binance/BinanceEventMapper.java`
* `src/main/java/com/quantlab/marketdata/connector/binance/BinanceTradeMessage.java`
* `src/main/java/com/quantlab/marketdata/connector/binance/BinanceKlineMessage.java`

这一层负责：

* 判断消息是否为有效业务消息
* 忽略订阅确认、控制类消息和额外字段
* 把 Binance DTO 转成统一领域事件

Review 重点：

* parser 和 mapper 是否分工明确
* 是否对真实 Binance 的非业务消息足够宽容
* DTO 是否只承担协议承接，不夹带业务逻辑

### 3.3 落库

核心文件：

* `src/main/java/com/quantlab/marketdata/connector/JdbcMarketDataEventPublisher.java`

这一层负责：

* 初始化 PostgreSQL schema
* 接收统一事件并写入数据库
* 对 `TradeEvent` / `KlineEvent` 做基础持久化
* 对 K 线按业务键做幂等写入

Review 重点：

* schema 初始化是否过重，未来是否要拆出独立迁移方式
* JDBC 代码是否仍保持“最小闭环”复杂度
* upsert、时间字段、方言兼容是否足够清楚

### 3.4 历史读取与窗口探查

核心文件：

* `src/main/java/com/quantlab/marketdata/history/MarketDataHistoryReader.java`
* `src/main/java/com/quantlab/marketdata/history/JdbcMarketDataHistoryReader.java`
* `src/main/java/com/quantlab/marketdata/history/MarketDataWindowInspector.java`
* `src/main/java/com/quantlab/marketdata/history/JdbcMarketDataWindowInspector.java`
* `src/main/java/com/quantlab/marketdata/history/KlineHistoryWindow.java`

这一层负责：

* 从 PostgreSQL 读取可回测历史 K 线
* 在不手写时间窗口时，自动反推出最近 N 根 K 线范围

Review 重点：

* `HistoryReader` 和 `WindowInspector` 的职责是否拆得合理
* `latest-bars` 这种配置方式是否比手写时间区间更适合当前阶段
* 时间边界定义是否统一：
  * `fromInclusive`
  * `toExclusive`

### 3.5 回测执行

核心文件：

* `src/main/java/com/quantlab/backtest/KlineBacktestEngine.java`
* `src/main/java/com/quantlab/backtest/BacktestRequest.java`
* `src/main/java/com/quantlab/backtest/BacktestResult.java`
* `src/main/java/com/quantlab/backtest/SimulatedBroker.java`
* `src/main/java/com/quantlab/backtest/Portfolio.java`
* `src/main/java/com/quantlab/backtest/BacktestMetrics.java`
* `src/main/java/com/quantlab/backtest/BacktestMetricsCollector.java`

这一层负责：

* 遍历历史 K 线
* 驱动策略信号执行
* 更新现金、持仓、权益
* 输出基础指标和回测结果

Review 重点：

* engine 是否只负责编排，不夹带太多策略逻辑
* broker / portfolio 的边界是否自然
* 当前指标是否够支撑下一阶段，而没有提前设计过度

### 3.6 策略与研究产物输出

核心文件：

* `src/main/java/com/quantlab/strategy/KlineStrategy.java`
* `src/main/java/com/quantlab/strategy/ClosePriceMomentumStrategy.java`
* `src/main/java/com/quantlab/analytics/BacktestResearchPipeline.java`
* `src/main/java/com/quantlab/analytics/BacktestResearchRunner.java`
* `src/main/java/com/quantlab/backtest/BacktestResultJsonExporter.java`
* `src/main/java/com/quantlab/analytics/BacktestResearchReportGenerator.java`
* `src/main/java/com/quantlab/analytics/BacktestResearchReportMarkdownExporter.java`

这一层负责：

* 提供最小策略实现
* 将回测结果导出为 JSON
* 生成中文研究报告
* 提供一次启动即可完成本地研究输出的 runner

Review 重点：

* `BacktestResearchRunner` 是否保持为“本地执行入口”，没有膨胀成业务核心
* Pipeline 是否承担“串联”职责，而不是吞掉全部细节
* 当前报告生成是否只是稳定出口，而不是过早做复杂 AI 集成

---

## 4. 本轮新增/关键能力

本轮尤其建议关注下面几项。

### 4.1 Binance 显式代理支持

相关配置：

* `quantlab.market-data.binance.proxy-enabled`
* `quantlab.market-data.binance.proxy-host`
* `quantlab.market-data.binance.proxy-port`

原因：

* 当前环境中 JVM 全局代理和系统代理继承并不稳定
* 显式配置更适合本地联调和排障

### 4.2 控制类消息兼容

已处理：

* 忽略没有 `e` 字段的控制类消息
* 忽略 `Trade / Kline` DTO 中额外字段

原因：

* 真实 Binance WebSocket 返回的并不全是业务事件
* 如果 parser 过严，联调时会出现大量非业务报错

### 4.3 `latest-bars` 自动回放

相关位置：

* `QuantLabProperties.BacktestRunProperties`
* `BacktestResearchRunner`
* `JdbcMarketDataWindowInspector`

作用：

* 可以直接从已落库历史里选最近 N 根 K 线
* 本地 review / demo / 回归时比手写时间窗口更方便

---

## 5. 建议你 Review 时重点判断的问题

建议每看完一层，只判断下面三个问题。

### 5.1 边界是否清楚

例如：

* connector 不应承担过多解析和持久化细节
* parser 不应承担事件发布职责
* runner 不应承载核心研究逻辑

### 5.2 配置是否收敛

例如：

* 有没有必须存在却命名不直观的配置
* 有没有只为一次联调而引入、但长期会污染接口的配置
* 默认值是否符合现在的真实使用方式

### 5.3 是否能自然承接 Phase 2

例如：

* 历史读取入口是否已经足够清楚
* 回测入口是否已具备稳定输入
* 还有没有必须先回头重构 Phase 1 才能继续的地方

---

## 6. 当前已知限制

这部分不是缺陷清单，而是当前阶段明确接受的限制。

### 6.1 持久化层仍然偏“最小实现”

现状：

* 使用原生 JDBC
* schema 初始化在应用内完成
* 尚未引入连接池、迁移框架、批量优化

结论：

* 当前对个人项目和闭环验证是够用的
* 不建议现在就为这些点发散

### 6.2 策略仍然是演示策略

现状：

* 当前主要用于验证回测与研究链路
* 不代表真实可交易策略质量

结论：

* Review 时应更多关注接口设计和链路稳定性
* 不要把当前收益指标直接当成策略能力结论

### 6.3 研究报告仍是规则生成

现状：

* 现在先生成规则版中文报告
* 目的是固定输入输出协议

结论：

* 这是给后续 `AI Research` 留稳定接口
* 不是最终 AI 能力形态

---

## 7. 建议的 Review 结果输出方式

你 review 时可以按下面四类记笔记，效率会比较高。

### 7.1 必改问题

会影响：

* 链路正确性
* 模块边界
* 后续开发方向

### 7.2 可改问题

会影响：

* 可读性
* 配置直觉
* 维护成本

### 7.3 暂不处理

典型包括：

* 性能优化
* 多交易所扩展
* 更重基础设施

### 7.4 下一阶段直接承接

例如：

* 回测执行边界进一步稳定
* 策略输入输出进一步抽象
* 指标和研究输出继续标准化

---

## 8. 本次交接的最终结论

当前仓库已经具备一个可以 Review 的最小研究平台雏形：

* 可以接 Binance 实时数据
* 可以写入 PostgreSQL
* 可以从数据库读历史 K 线
* 可以驱动最小回测
* 可以输出 JSON 和中文研究报告

因此下一步最合理的动作不是继续铺新功能，而是：

* 完成这一轮 Review
* 修掉边界和可读性问题
* 再进入 `Phase 2 - Backtest Core` 的收敛推进
