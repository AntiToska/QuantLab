# QuantLab 项目看板

> 项目管理看板

---

# 当前状态

版本：`v0.1.0`

当前里程碑：

`Market Data`

当前文档：

* `doc/DEV_LOG.md`
* `doc/WEEKLY_REVIEW.md`

---

# 待办清单

## 基础设施

* [x] 初始化 Spring Boot 工程
* [ ] Docker Compose 基础环境
* [ ] PostgreSQL 部署与初始化
* [ ] 配置管理收敛
* [ ] 基础运行脚本与开发说明

---

## Market Data

* [x] Binance WebSocket Connector 基础链路
* [ ] Binance 真实消息消费联调
* [x] Market Data Event Model
* [ ] Trade 数据落库
* [ ] Kline 数据落库
* [ ] OrderBook Snapshot 数据落库
* [ ] OKX WebSocket Connector
* [ ] Bybit WebSocket Connector

---

## Backtest Core

* [ ] 历史数据加载器
* [ ] 事件驱动回测循环
* [ ] Broker Simulator
* [ ] Position / Portfolio 演算
* [ ] 回测结果结构化输出

---

## Strategy + Metrics

* [ ] Strategy Interface
* [ ] Signal Model
* [ ] 简单均线策略
* [ ] Momentum 策略
* [ ] 绩效指标计算
* [ ] Sharpe / Drawdown / WinRate 输出

---

## AI Research

* [ ] 回测结果 JSON 结构定义
* [ ] LLM 分析输入适配
* [ ] 策略分析报告生成
* [ ] 风险提示与优化建议输出

---

## Paper Trading / Risk

* [ ] 虚拟账户
* [ ] 模拟下单与成交
* [ ] 持仓跟踪
* [ ] Position Limit
* [ ] Max Drawdown Control
* [ ] Daily Loss Limit

---

## Performance Scaling

* [ ] ClickHouse 评估与接入
* [ ] Kafka 事件总线评估
* [ ] Redis 缓存 / 状态层评估
* [ ] Prometheus / Grafana 监控接入

---

## Dashboard

* [ ] Equity Curve
* [ ] Drawdown Chart
* [ ] Position Dashboard
* [ ] Trade History

---

# 路线图

## Phase 1: Market Data

目标：

先打通 Binance WebSocket、统一事件模型和基础落库。

任务：

* [x] Binance WebSocket 基础接入
* [x] 统一行情事件模型
* [ ] PostgreSQL 基础落库
* [ ] 历史归档与回放入口

完成标准：

* 可以采集实时行情
* 可以将核心数据归档
* 可以为后续回测提供输入

---

## Phase 2: Backtest Core

目标：

先形成最小可运行的事件驱动回测闭环。

任务：

* [ ] 历史数据加载
* [ ] 回测事件循环
* [ ] Broker Simulator
* [ ] Portfolio Engine

完成标准：

* 策略能在历史数据上运行
* 基础收益结果可以输出

---

## Phase 3: Strategy + Metrics

目标：

补齐最小策略层与绩效指标层。

任务：

* [ ] 统一策略接口
* [ ] 示例策略
* [ ] Sharpe / Drawdown / WinRate
* [ ] 结构化回测结果输出

完成标准：

* 至少一个示例策略可运行
* 可以稳定输出结构化绩效结果

---

## Phase 4: AI Research

目标：

基于回测结果做 LLM 分析与报告生成。

任务：

* [ ] 回测结果 JSON 输出协议
* [ ] LLM 分析输入
* [ ] 中文分析报告生成
* [ ] 风险与优化建议生成

完成标准：

* 输入回测结果后可自动生成研究报告

---

## Phase 5: Paper Trading / Risk

目标：

将策略推进到仿真交易与基础风控阶段。

任务：

* [ ] 虚拟账户
* [ ] 实时模拟交易
* [ ] 持仓跟踪
* [ ] 风控规则

完成标准：

* 可持续运行模拟交易闭环

---

## Phase 6: Performance Scaling

目标：

在前面能力稳定后，再推进基础设施扩展。

任务：

* [ ] ClickHouse 分析型存储
* [ ] Kafka 事件总线
* [ ] Redis 缓存层
* [ ] 监控指标体系

完成标准：

* 在不破坏核心业务模型的前提下完成性能扩展

---

# 后续想法

* 多资产组合回测
* 期货与永续合约支持
* Funding Rate 策略
* 因子研究平台
* AI 策略分析助手
* 强化学习研究
* 跨交易所套利
* 做市仿真
* OrderBook 回放系统
* 高频研究工具链
