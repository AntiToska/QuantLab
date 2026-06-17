# QuantLab 项目看板

> 项目的单一阶段管理文档

这个文档同时承担：

* 当前阶段说明
* 下一阶段目标管理
* 中期路线图维护
* 任务清单维护
* 暂缓项记录

---

# 当前状态

版本：`v0.1.0`

当前阶段：`Phase 1 - Market Data`

当前工作重点：

* 用真实 Binance 落库数据验证 `HistoryReader -> Backtest -> Report` 的完整研究闭环
* 将当前真实采集能力收敛为稳定可复用的本地联调入口
* 保持研究闭环收敛，不提前扩展无关基础设施

最近已完成：

* Spring Boot 工程骨架
* Market Data 统一事件模型
* Connector 生命周期管理骨架
* Binance 消息解析与订阅建模
* WebSocket transport 抽象
* 真实 WebSocket 的重连、订阅重放与心跳基础能力
* PostgreSQL 基础落库入口
* 历史行情读取入口
* 本地 PostgreSQL Docker 联调
* 最小 K 线回测闭环
* 示例收盘价动量策略
* 基础回测指标输出
* 回测结果 JSON 导出能力
* 规则版中文研究报告生成
* 最小研究流水线入口
* 基础测试体系与回归验证
* 本地实时采集 runner
* Binance 显式代理接入
* 真实 Binance + PostgreSQL 联调成功

相关文档：

* `doc/DEV_LOG.md`
* `doc/WEEKLY_REVIEW.md`
* `doc/AGENT_GUIDE.md`

---

# 当前阶段目标

## 阶段名称

`Phase 1 - Market Data`

## 阶段目标

先打通 Binance WebSocket、统一事件模型和基础落库，形成可归档、可回放、可供后续回测消费的数据入口。

## 当前阶段范围

本阶段应该完成：

* Binance 实时行情接入
* Trade / Kline / OrderBook 等统一事件建模
* 基础落库能力
* 历史归档与回放入口的雏形

本阶段不追求：

* 多交易所全部打通
* 高性能分析型存储
* 复杂缓存层
* 分布式事件总线

## 当前阶段完成标准

满足以下条件即可视为当前阶段完成：

* 能稳定接收 Binance 实时行情
* 能将核心行情数据写入 PostgreSQL
* 能为后续回测提供基础历史数据输入
* 关键链路具备基础测试和回归验证
* 至少完成一次真实 Binance 采集成功验证

---

# 下一阶段目标

## 阶段名称

`Phase 2 - Backtest Core`

## 前置条件

进入下一阶段前，需要至少满足：

* Market Data 基础落库已打通
* 历史数据读取入口明确
* 统一事件模型可以被回测模块复用

## 下一阶段目标

先形成最小可运行的事件驱动回测闭环。

## 下一阶段重点

* 历史数据加载
* 回测事件循环
* Broker Simulator
* Portfolio Engine
* 基础回测结果输出

## 下一阶段完成标准

* 至少一个简单策略可以在历史数据上运行
* 可以稳定输出基础收益结果
* 回测结果具备进入 `Strategy + Metrics` 和 `AI Research` 的结构化基础

---

# 路线图

## Phase 1: Market Data

目标：

先打通 Binance WebSocket、统一事件模型和基础落库。

任务：

* [x] Binance WebSocket 基础接入
* [x] 统一行情事件模型
* [x] PostgreSQL 基础落库
* [x] 历史归档与回放入口
* [x] 本地实时采集验证入口

完成标准：

* 可以采集实时行情
* 可以将核心数据归档
* 可以为后续回测提供输入

---

## Phase 2: Backtest Core

目标：

先形成最小可运行的事件驱动回测闭环。

任务：

* [x] 历史数据加载
* [x] 回测事件循环
* [x] Broker Simulator
* [x] Portfolio Engine

完成标准：

* 策略能在历史数据上运行
* 基础收益结果可以输出

---

## Phase 3: Strategy + Metrics

目标：

补齐最小策略层与绩效指标层。

任务：

* [x] 统一策略接口
* [x] 示例策略
* [x] Sharpe
* [x] Drawdown / WinRate
* [x] 结构化回测结果输出

完成标准：

* 至少一个示例策略可运行
* 可以稳定输出结构化绩效结果

---

## Phase 4: AI Research

目标：

基于回测结果做 LLM 分析与报告生成。

任务：

* [x] 回测结果 JSON 输出协议
* [x] LLM 分析输入
* [x] 中文分析报告生成
* [x] 风险与优化建议生成

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

# Backlog

## 基础设施

* [x] 初始化 Spring Boot 工程
* [x] Docker Compose 基础环境
* [x] PostgreSQL 部署与初始化
* [ ] 配置管理收敛
* [ ] 基础运行脚本与开发说明

---

## Market Data

* [x] Binance WebSocket Connector 基础链路
* [ ] Binance 真实消息消费联调
* [x] Market Data Event Model
* [x] Trade 数据落库
* [x] Kline 数据落库
* [ ] OrderBook Snapshot 数据落库

---

## Backtest Core

* [x] Market Data 历史读取入口
* [x] Backtest 历史数据加载器
* [x] 事件驱动回测循环
* [x] Broker Simulator
* [x] Position / Portfolio 演算
* [ ] 回测结果结构化输出

---

## Strategy + Metrics

* [x] Kline Strategy Interface
* [x] Signal Model
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

# 暂缓项

当前明确暂缓，不作为近期开发目标：

* OKX WebSocket Connector
* Bybit WebSocket Connector
* ClickHouse 正式接入
* Kafka 事件总线落地
* Redis 状态层与缓存层
* Kubernetes 与微服务拆分
* 复杂监控平台
* 高频场景专项优化

这些方向仍然有价值，但必须放在 `Binance -> PostgreSQL -> Backtest -> Strategy -> Metrics -> AI Report` 这条核心研究闭环稳定之后再考虑。

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
