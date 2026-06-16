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
* 基础测试体系

项目开发记录见：

* [开发日志](doc/DEV_LOG.md)
* [周报](doc/WEEKLY_REVIEW.md)
* [项目看板](doc/PROJECT_BOARD.md)
* [Agent 开发约束](doc/AGENT_GUIDE.md)

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
