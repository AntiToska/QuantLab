# QuantLab Project Board

> Project Management Board

---

# Current Status

Version: v0.1.0

Current Milestone:

Market Data Infrastructure

Documentation:

* `doc/DEV_LOG.md`
* `doc/WEEKLY_REVIEW.md`

---

# Backlog

## Infrastructure

* [ ] Initialize Spring Boot Project
* [ ] Docker Compose Environment
* [ ] ClickHouse Deployment
* [ ] PostgreSQL Deployment
* [ ] Configuration Management

---

## Market Data

* [ ] Binance WebSocket Connector
* [ ] OKX WebSocket Connector
* [ ] Market Data Event Model
* [ ] Tick Data Storage
* [ ] Kline Data Storage
* [ ] OrderBook Snapshot Storage

---

## Strategy Framework

* [ ] Strategy Interface
* [ ] Signal Model
* [ ] Strategy Registry
* [ ] Strategy Lifecycle

---

## Backtesting Engine

* [ ] Event Engine
* [ ] Historical Data Loader
* [ ] Broker Simulator
* [ ] Position Management
* [ ] Portfolio Engine
* [ ] Performance Metrics

---

## Risk Management

* [ ] Position Limit
* [ ] Max Drawdown Control
* [ ] Daily Loss Limit
* [ ] Risk Alert

---

## Paper Trading

* [ ] Virtual Account
* [ ] Order Simulator
* [ ] Position Tracking
* [ ] Real-Time Trading Loop

---

## Dashboard

* [ ] Equity Curve
* [ ] Drawdown Chart
* [ ] Position Dashboard
* [ ] Trade History

---

## AI Assistant

* [ ] Strategy Explanation
* [ ] Backtest Report Analysis
* [ ] Risk Diagnosis
* [ ] Strategy Suggestion

---

# Milestone 1

Market Data Infrastructure

Goal:

Build a stable market data collection system.

Tasks:

* [ ] Binance WebSocket
* [ ] Event Model
* [ ] ClickHouse Storage
* [ ] Historical Replay

Exit Criteria:

* Real-time market data can be collected
* Data can be persisted
* Historical data can be queried

---

# Milestone 2

Backtesting Engine

Goal:

Build a generic event-driven backtest engine.

Tasks:

* [ ] Event Engine
* [ ] Broker Simulator
* [ ] Portfolio Engine
* [ ] Metrics Engine

Exit Criteria:

* Strategy can run on historical data
* Metrics can be generated

---

# Milestone 3

Strategy Framework

Goal:

Provide pluggable strategy architecture.

Tasks:

* [ ] Strategy Interface
* [ ] MA Strategy
* [ ] Momentum Strategy
* [ ] Grid Strategy

Exit Criteria:

* Multiple strategies supported

---

# Milestone 4

Paper Trading

Goal:

Validate strategies in live market environments.

Tasks:

* [ ] Virtual Trading
* [ ] Position Tracking
* [ ] Risk Control

Exit Criteria:

* Simulated trading can run continuously

---

# Milestone 5

QuantLab MVP

Goal:

Release first usable version.

Tasks:

* [ ] Dashboard
* [ ] Risk Module
* [ ] Documentation
* [ ] Deployment Guide

Exit Criteria:

* Public GitHub Release
* Version v1.0.0
* Complete README
* Docker Deployment
* Sample Strategy Included

---

# Future Ideas

* Multi-Asset Portfolio
* Futures Trading
* Funding Rate Strategy
* Factor Research Platform
* AI Strategy Assistant
* Reinforcement Learning
* Multi-Exchange Arbitrage
* Market Making Simulator
* OrderBook Replay System
* High Frequency Research Toolkit
