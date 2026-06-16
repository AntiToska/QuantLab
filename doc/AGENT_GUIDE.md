# QuantLab Agent Guide

> Instructions for AI coding agents

This document defines the delivery rules for all AI coding agents working on QuantLab.

Applicable to:

* Claude Code
* Codex
* Cursor Agent
* Cline
* Roo Code
* GitHub Copilot Agent

---

# Project Vision

QuantLab is a personal quantitative research platform.

Primary goals:

* Learn quantitative trading system design
* Practice event-driven architecture
* Build a production-style backend project
* Support backtesting and paper trading
* Explore AI-assisted quantitative research early

This project is NOT intended to become a real-money trading bot.

Engineering quality is more important than trading profitability.

---

# Development Philosophy

Priority order:

```text
Readability
    >
Maintainability
    >
Correctness
    >
Performance
```

Avoid premature optimization.

Build working systems first.

---

# Architecture Principles

## Event Driven

Core modules should communicate through events.

Example:

Market Data Event
->
Strategy Event
->
Order Event
->
Trade Event
->
Portfolio Event

Avoid direct coupling between modules.

---

## Modular Design

Every module must have clear boundaries.

Allowed modules:

* market-data
* strategy
* backtest
* risk
* simulator
* analytics
* dashboard
* ai-assistant

Do not create new top-level modules without approval.

---

## Dependency Direction

Allowed:

Market Data
->
Strategy
->
Backtest

Not allowed:

Strategy
->
Market Data

Risk
->
Dashboard

Dashboard
->
Core Engine

---

# Coding Rules

## Language

Java 21

Preferred features:

* record
* sealed interface
* Optional
* Stream API in moderation

Avoid:

* reflection-heavy design
* dynamic code generation

---

## Framework

Spring Boot 3

Preferred:

Constructor injection

Avoid field injection.

---

## Package Structure

Required:

```text
com.quantlab

├── marketdata
├── strategy
├── backtest
├── risk
├── simulator
├── analytics
└── common
```

Do not create deep package nesting.

Maximum depth:

4

---

# Database Rules

Current phase primary database:

PostgreSQL

Future optional database:

ClickHouse

Rules:

* Agents MUST use PostgreSQL as the default persistence target in the current phase
* Agents MUST NOT introduce ClickHouse unless the user explicitly asks for performance-scaling work
* Agents MUST NOT add Kafka
* Agents MUST NOT add Redis
* If a document mentions Kafka, Redis, or ClickHouse, treat them as future options unless the user says otherwise

---

# Logging Rules

Use:

SLF4J

Required:

* startup logs
* error logs
* important business logs

Avoid:

System.out.println()

---

# Testing Rules

Every new feature must include tests.

Preferred:

JUnit 5

Minimum:

* service layer tests
* utility tests

Target:

strong coverage on core modules

---

# Git Workflow

Main branch:

`main`

Feature branch:

`task/...`

Agents should commit in meaningful increments with clear messages.

---

# Roadmap Alignment

Agents should align implementation suggestions with this staged roadmap:

1. Market Data
   Focus on Binance WebSocket, normalized event models, and basic persistence.
2. Backtest Core
   Build the minimum event-driven backtest loop on historical or archived data.
3. Strategy + Metrics
   Add simple strategies, performance metrics, and structured outputs.
4. AI Research
   Add LLM-based analysis for backtest outputs and report generation early.
5. Paper Trading / Risk
   Extend into simulated execution and risk controls after the core loop is stable.
6. Performance Scaling
   Consider ClickHouse, Kafka, Redis, and heavier infrastructure only after the earlier phases are working.

Agents MUST prefer simpler implementations that help complete the current phase.
