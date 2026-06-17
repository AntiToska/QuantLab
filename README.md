# QuantLab

> 面向个人开发者的量化研究与回测平台

QuantLab 是一个以学习和工程实践为导向的个人量化项目。

它的目标不是做“自动赚钱机器人”，而是逐步构建一套具备真实工程价值的量化研究平台，重点覆盖：

* 实时行情采集
* 事件驱动架构
* 历史数据归档
* 回测引擎
* 策略开发
* 模拟交易与风控
* AI 辅助研究

---

## 项目定位

很多开源量化项目要么停留在简单示例，要么过早堆复杂基础设施，导致个人开发者很难持续推进。

QuantLab 采用更适合个人项目的路线：

1. 先打通最小可运行闭环
2. 先保证模型、流程和边界清晰
3. 再逐步扩展性能、存储和多交易所能力

这个项目更关注：

* 是否能稳定采集和消费行情事件
* 是否能用统一模型串起实时链路与回测链路
* 是否能形成可分析、可展示、可演进的研究工作流

---

## 当前阶段的核心技术

当前明确采用的技术：

* Java 21
* Spring Boot 3
* PostgreSQL
* WebSocket
* JUnit 5

当前阶段刻意不引入：

* Kafka
* Redis
* ClickHouse

这些技术不是永远不用，而是暂时不作为当前里程碑的落地范围。现阶段更重要的是先完成功能闭环，而不是过早优化架构复杂度。

---

## 未来可演进技术

在后续需要扩展吞吐、缓存、分析能力时，可以再评估：

* ClickHouse
* Kafka
* Redis
* Prometheus
* Grafana

这些都属于后续性能与基础设施扩展项，不属于当前 MVP 的前置要求。

---

## 核心能力规划

### 1. Market Data

先打通交易所行情接入与内部标准化事件流。

当前重点：

* Binance WebSocket 接入
* Trade / Kline / OrderBook 等统一事件模型
* 基础行情归档
* 多交易所扩展边界预留

### 2. Backtest Core

先完成最小可运行回测闭环。

目标包括：

* 历史数据加载
* 事件驱动回测循环
* 基础撮合与持仓演算
* 回测结果输出

### 3. Strategy + Metrics

先具备最基础的策略执行与绩效分析能力。

目标包括：

* 统一策略接口
* 简单示例策略
* 收益、回撤、Sharpe、胜率等指标
* 结构化结果输出

### 4. AI Research

尽早把 AI 放进研究链路，而不是最后再补一个“助手外壳”。

最小版本目标：

* 接收回测结果 JSON
* 调用 LLM 生成策略分析报告
* 输出风险提示与优化建议

示例输入：

```json
{
  "sharpe": 1.5,
  "maxDrawdown": 12,
  "winRate": 48
}
```

示例输出方向：

* 策略收益较稳定
* 最大回撤略高
* 建议增加止损约束或优化仓位控制

### 5. Paper Trading / Risk

在回测和策略分析稳定后，再推进更接近真实运行环境的能力。

目标包括：

* 虚拟账户
* 模拟下单与成交
* 持仓跟踪
* 风控规则

### 6. Performance Scaling

性能扩展放在后面做，而不是一开始就让项目被基础设施复杂度拖住。

可能的扩展方向：

* ClickHouse 分析型存储
* Kafka 事件总线
* Redis 缓存与状态加速
* 更完整的监控与指标体系

---

## 推荐实现路线

当前建议按下面的顺序推进：

1. Market Data
   先打通 Binance WebSocket、事件模型、基础落库。
2. Backtest Core
   先做最小回测闭环，能消费历史/归档数据。
3. Strategy + Metrics
   先有简单策略、绩效指标、结果结构化输出。
4. AI Research
   基于回测结果做 LLM 报告生成与分析建议。
5. Paper Trading / Risk
   再往仿真交易和风控推进。
6. Performance Scaling
   最后再考虑 ClickHouse、Kafka、Redis 这类扩展。

---

## 项目结构

当前代码采用单体工程逐步演进，核心包结构如下：

```text
com.quantlab
├── common
├── marketdata
├── strategy
├── backtest
├── risk
├── simulator
└── analytics
```

设计原则：

* 先按领域边界拆包
* 先保证模块职责清晰
* 暂不为了“看起来高级”而过早拆成多仓或微服务

---

## 当前进展

已完成的基础工作包括：

* Spring Boot 工程骨架
* Market Data 统一事件模型
* Connector 生命周期管理骨架
* Binance 消息解析与订阅建模
* WebSocket transport 抽象
* 真实 WebSocket 的重连、订阅重放与心跳基础能力
* 可选的 PostgreSQL JDBC 落库发布器
* 基础测试体系

项目开发记录见：

* [开发日志](doc/DEV_LOG.md)
* [周报](doc/WEEKLY_REVIEW.md)
* [项目看板](doc/PROJECT_BOARD.md)
* [Agent 开发约束](doc/AGENT_GUIDE.md)

---

## Market Data 持久化配置

当前 `market-data` 模块支持两种事件发布模式：

* 默认模式：仅打印日志，不连接数据库
* 持久化模式：将统一行情事件写入 PostgreSQL

对应配置项如下：

```yaml
quantlab:
  market-data:
    persistence:
      enabled: false
      jdbc-url: jdbc:postgresql://localhost:5432/quantlab
      username: quantlab
      password: quantlab
```

配置说明：

* `enabled`
  控制是否启用 PostgreSQL 持久化；默认 `false`
* `jdbc-url`
  PostgreSQL JDBC 连接串
* `username`
  数据库用户名
* `password`
  数据库密码

当前已支持落库的事件类型：

* `TradeEvent`
* `KlineEvent`

当前暂未落库：

* `OrderBookSnapshotEvent`

---

## Research 输出

当前 `analytics` 模块已经具备最小研究输出链路：

```text
BacktestResult
  -> backtest-result.json
  -> BacktestResearchReport
  -> research-report.md
```

如果你已经准备好了 `BacktestRequest`、`KlineStrategy` 和历史数据来源，可以直接调用：

* `BacktestResearchPipeline.run(...)`
  返回内存中的完整研究产物
* `BacktestResearchPipeline.runAndWrite(...)`
  将结果写到指定目录

默认会输出两个文件：

* `backtest-result.json`
* `research-report.md`

这个入口的目标是为后续真实联调提供稳定出口，而不是先引入复杂的 Web、CLI 或任务调度框架。

如果你想在本地用 PostgreSQL 历史数据直接跑一次研究输出，可以开启：

```yaml
quantlab:
  research:
    seed-data:
      enabled: true
      exchange: BINANCE
      symbol: BTCUSDT
      interval: ONE_MINUTE
      from-inclusive: 2026-06-17T00:00:00Z
      bars: 120
      start-price: "100"
      price-step: "1"
      volume: "10"
  market-data:
    persistence:
      enabled: true
  research:
    backtest-run:
      enabled: true
      exchange: BINANCE
      symbol: BTCUSDT
      interval: ONE_MINUTE
      from-inclusive: 2026-06-17T00:00:00Z
      to-exclusive: 2026-06-17T01:00:00Z
      strategy: close-price-momentum
      output-directory: ./output/research
      initial-cash: "10000"
      trade-quantity: "1"
```

如果本地库里还没有可回测的历史 K 线，可以先开启 `seed-data.enabled=true` 灌入一小段样例数据。

启动后会按顺序：

1. 灌入样例 K 线数据（如果开启）
2. 执行一次研究流水线
3. 将结果写到 `output-directory`

---

## 本地 PostgreSQL 联调

如果你想在本机联调 `Binance -> PostgreSQL` 这条链路，可以按下面做。

### 1. 用 Docker 启动 PostgreSQL

项目根目录已经提供 `compose.yaml`，默认会启动一个本地 PostgreSQL：

```bash
docker compose up -d postgres
```

默认连接信息：

```text
host: localhost
port: 5432
database: quantlab
username: quantlab
password: quantlab
```

查看容器状态：

```bash
docker compose ps
```

停止数据库：

```bash
docker compose down
```

如果需要连数据卷一起清掉：

```bash
docker compose down -v
```

如果你没有使用 Docker，也可以手动创建数据库和用户：

```sql
create database quantlab;
create user quantlab with password 'quantlab';
grant all privileges on database quantlab to quantlab;
```

### 2. 打开持久化配置

把 `src/main/resources/application.yml` 或你自己的本地覆盖配置改成：

```yaml
quantlab:
  market-data:
    persistence:
      enabled: true
      jdbc-url: jdbc:postgresql://localhost:5432/quantlab
      username: quantlab
      password: quantlab
```

### 3. 选择 WebSocket 模式

如果你只想验证数据库写入链路，可以继续保持 stub 模式：

```yaml
quantlab:
  market-data:
    binance:
      real-client-enabled: false
```

如果你要联调真实 Binance WebSocket，则改成：

```yaml
quantlab:
  market-data:
    binance:
      real-client-enabled: true
```

### 4. 启动应用

```bash
mvn spring-boot:run
```

默认情况下，持久化发布器会在启动时自动建表。

当前会自动创建：

* `market_data_trades`
* `market_data_klines`

### 5. 验证数据

启动后可以在 PostgreSQL 中查询：

```sql
select * from market_data_trades order by id desc limit 20;
select * from market_data_klines order by id desc limit 20;
```

如果你开启的仍是 stub 模式，那么只会验证应用装配和数据库连通性，不会自动产生真实行情写入。  
要看到真实数据，需要同时开启真实 Binance WebSocket。

### 6. 当前限制

这套持久化实现目前是“先跑通闭环”的最小版本：

* 使用原生 JDBC，而不是 ORM
* 每次发布事件单独建连写入，优先保证简单可控
* 暂未做批量写入、连接池、重试和分区策略
* 暂未持久化 OrderBookSnapshot

这些能力会放到后续性能优化阶段再做。

---

## 长期目标

QuantLab 的长期目标不是成为真实资金交易机器人，而是成为一套适合个人开发者持续打磨的量化研究平台。

它希望帮助开发者同时学习和实践：

* 量化系统设计
* 事件驱动架构
* 实时数据处理
* 回测与仿真交易
* AI 辅助研究能力

最终形成一个既能展示工程能力，也能展示研究能力的完整项目。
