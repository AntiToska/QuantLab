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
