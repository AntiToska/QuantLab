# QuantLab Weekly Review

> Weekly Development Summary

---

## Week Of 2026-06-15

### 本周概览

本周完成了 QuantLab 项目的初始工程搭建，并正式进入 `Market Data Infrastructure` 里程碑的第一阶段。工作重点放在基础工程、统一领域模型、连接器抽象，以及 Binance 协议解析链路的初版实现。

### 本周完成

* 建立 Spring Boot 单体工程骨架
* 完成 Market Data 统一事件模型
* 完成 Connector 生命周期管理骨架
* 完成 Binance `trade / kline` 消息解析与映射
* 完成真实 WebSocket transport 的重连、订阅重放与心跳基础能力
* 建立基础测试体系并跑通本地构建

### 本周产出

* 基础工程可编译、可测试
* `marketdata` 模块已经有稳定的内部边界
* Binance 接入已具备“订阅建模 + 原始消息解析 + 标准化事件发布”的基础能力
* 真实 WebSocket 接入已经具备基础恢复能力，可为后续联调做准备

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

### 本周问题

* WSL / 受管环境网络状态不一致，影响 Maven 依赖拉取稳定性
* 早期路线对 ClickHouse 等组件着墨过多，不适合当前个人项目阶段

### 下周重点

* 推进 PostgreSQL 基础落库
* 为 Backtest Core 设计最小历史数据加载与事件回放入口
* 评估 AI Research 的最小报告生成链路
