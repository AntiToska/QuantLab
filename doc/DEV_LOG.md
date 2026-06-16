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

#### 4. Git 记录

* 完成并推送提交：
  * `b071524 feat(binance): add reconnect and heartbeat to websocket transport`

### 今日问题

* 之前的 README 与 Agent Guide 在技术选型上存在冲突，容易误导后续实现
* 早期路线过度强调 ClickHouse 等基础设施，偏离了当前最重要的学习与闭环目标

### 下一步

* 开始推进 PostgreSQL 基础落库
* 为回测核心设计最小历史数据加载与事件回放入口
* 评估 AI Research 最小输入输出协议
