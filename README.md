# QuantLab

> A Quantitative Research & Backtesting Platform for Individual Developers

QuantLab 是一个面向个人开发者和量化交易爱好者的量化研究平台。

项目聚焦于：

* 实时行情采集
* 历史数据管理
* 策略开发
* 回测引擎
* 模拟交易
* 风险控制
* 策略分析
* AI 辅助研究

目标不是提供自动赚钱机器人，而是构建一个具备真实工程价值的量化研究平台。

---

## Why QuantLab

大多数开源量化项目存在以下问题：

* 仅支持简单回测
* 缺少完整风控体系
* 缺少实时数据处理能力
* 无法模拟真实交易环境
* 工程架构较弱

QuantLab 希望提供：

```text
Market Data
     ↓
Strategy
     ↓
Backtest
     ↓
Risk Control
     ↓
Paper Trading
     ↓
Analytics
```

完整的量化研究工作流。

---

## Core Features

### Market Data

实时行情采集

支持：

* Binance
* OKX
* Bybit

功能：

* WebSocket 实时订阅
* K线数据
* Trade 数据
* OrderBook 数据
* Tick 数据

---

### Historical Data Storage

统一历史数据存储。

支持：

* ClickHouse
* PostgreSQL

数据类型：

* OHLCV
* Trades
* OrderBook Snapshot
* Funding Rate

---

### Strategy Framework

统一策略开发接口。

示例：

```java
public interface Strategy {

    Signal onBar(BarData bar);

}
```

支持：

* Moving Average
* Momentum
* Grid Trading
* Mean Reversion

后续支持：

* Multi-Factor
* Machine Learning
* Reinforcement Learning

---

### Backtesting Engine

支持：

* 单资产回测
* 多资产回测
* 手续费模拟
* 滑点模拟
* 仓位管理

输出指标：

* PnL
* Annual Return
* Sharpe Ratio
* Max Drawdown
* Win Rate

---

### Risk Management

风控模块。

支持：

* 最大仓位限制
* 最大回撤限制
* 单日亏损限制
* 熔断机制
* 杠杆限制

---

### Paper Trading

模拟交易系统。

支持：

* 虚拟账户
* 实时行情驱动
* 模拟成交
* 持仓管理
* 资金管理

目标：

在真实市场环境下验证策略。

---

### Analytics Dashboard

策略分析面板。

展示：

* 资金曲线
* 回撤曲线
* 持仓变化
* 收益统计
* 风险指标

---

### AI Research Assistant

未来规划。

结合 LLM：

* 策略解释
* 回测分析
* 风险诊断
* 策略生成建议

支持：

* OpenAI
* Claude
* DeepSeek
* Dify

---

## Project Structure

```text
quantlab
│
├── market-data
│   ├── websocket
│   ├── collector
│   └── storage
│
├── strategy
│   ├── ma-cross
│   ├── momentum
│   ├── grid
│   └── mean-reversion
│
├── backtest
│   ├── engine
│   ├── broker
│   └── portfolio
│
├── risk
│   ├── exposure
│   ├── stop-loss
│   └── circuit-breaker
│
├── simulator
│   └── paper-trading
│
├── analytics
│   ├── pnl
│   ├── sharpe
│   └── drawdown
│
├── dashboard
│
└── ai-assistant
```

---

## Technology Stack

Backend

* Java 21
* Spring Boot 3

Data

* ClickHouse
* PostgreSQL
* Redis

Messaging

* Kafka

Infrastructure

* Docker
* Docker Compose

Monitoring

* Prometheus
* Grafana

AI

* OpenAI Compatible API
* Dify

---

## Roadmap

### Phase 1

Market Data Infrastructure

* [ ] Binance WebSocket
* [ ] OKX WebSocket
* [ ] ClickHouse Storage

---

### Phase 2

Backtesting Engine

* [ ] Event Engine
* [ ] Broker Simulator
* [ ] Portfolio Engine

---

### Phase 3

Strategy Framework

* [ ] MA Strategy
* [ ] Grid Strategy
* [ ] Momentum Strategy

---

### Phase 4

Risk Management

* [ ] Position Limit
* [ ] Drawdown Limit
* [ ] Daily Loss Limit

---

### Phase 5

Paper Trading

* [ ] Virtual Account
* [ ] Real-time Simulation
* [ ] Performance Tracking

---

### Phase 6

Analytics Dashboard

* [ ] Equity Curve
* [ ] Risk Report
* [ ] Strategy Comparison

---

### Phase 7

AI Assistant

* [ ] Strategy Explanation
* [ ] Backtest Analysis
* [ ] Strategy Optimization Suggestions

---

## Long-Term Vision

QuantLab 的长期目标不是成为交易机器人。

而是成为：

> A Personal Quantitative Research Platform

帮助开发者学习：

* Quantitative Trading
* Event-Driven Architecture
* Real-Time Data Processing
* Risk Management
* AI-Assisted Research

同时构建具有真实工程价值的开源项目。
