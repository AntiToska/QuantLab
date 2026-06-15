# QuantLab Agent Guide

> Instructions for AI Coding Agents

This document defines the development rules for all AI coding agents working on QuantLab.

Applicable to:

* Claude Code
* Codex
* Cursor Agent
* Cline
* Roo Code
* GitHub Copilot Agent

---

# Project Vision

QuantLab is a quantitative research platform.

Primary goals:

* Learn quantitative trading systems
* Practice event-driven architecture
* Build a production-style backend project
* Support backtesting and paper trading
* Explore AI-assisted quantitative research

This project is NOT intended to become a real-money trading bot.

Engineering quality is more important than trading profitability.

---

# Development Philosophy

Priority Order:

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

Core modules communicate through events.

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

Not Allowed:

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
* Stream API (moderately)

Avoid:

* reflection-heavy design
* dynamic code generation

---

## Framework

Spring Boot 3

Preferred:

Constructor Injection

Example:

```java
@Service
public class MarketDataService {

    private final EventPublisher publisher;

    public MarketDataService(EventPublisher publisher) {
        this.publisher = publisher;
    }

}
```

Avoid:

```java
@Autowired
private EventPublisher publisher;
```

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

Primary Database:

ClickHouse

Secondary Database:

PostgreSQL

Rules:

* Use ClickHouse for market data
* Use PostgreSQL for metadata
* Do not mix responsibilities

---

# Logging Rules

Use:

SLF4J

Required:

* Startup logs
* Error logs
* Important business logs

Avoid:

System.out.println()

---

# Testing Rules

Every new feature must include tests.

Preferred:

JUnit 5

Minimum:

* Service layer tests
* Utility tests

Target:

80%+ coverage for core modules

---

# Git Workflow

Main Branch:

main

Feature Branch:

feature/*

Task Branch:

task/*

Examples:

feature/backtest-engine

task/11-event-engine

task/12-broker-simulator

Never commit directly to main.

---

# Pull Request Rules

Keep PR small.

Target:

< 20 files changed

Preferred:

< 500 lines changed

Avoid giant PRs.

---

# Agent Restrictions

Agents MUST NOT:

* Introduce new frameworks
* Change database technology
* Change package structure
* Introduce microservices
* Add Kafka
* Add Kubernetes
* Add Redis

Unless explicitly requested.

QuantLab starts as a modular monolith.

---

# Current Technical Scope

Allowed:

* Spring Boot
* ClickHouse
* PostgreSQL
* Docker

Not Yet Allowed:

* Kafka
* Redis
* Elasticsearch
* Kubernetes

Keep architecture simple.

---

# Documentation Rules

Every major feature requires:

* Design Description
* Sequence Diagram
* Usage Example

Location:

docs/

---

# Milestone Development Strategy

Before writing code:

1. Understand milestone goal
2. Create implementation plan
3. Identify affected modules
4. Generate tests
5. Implement feature
6. Update documentation

---

# Definition of Done

A task is complete only if:

* Code compiles
* Tests pass
* Documentation updated
* No TODO left behind
* No dead code
* No commented-out code

---

# Long-Term Goal

Build a clean and extensible quantitative research platform.

Do not optimize for speed of development.

Optimize for:

* Maintainability
* Learnability
* Architecture Quality
