# Apex Exchange Engine

## Overview

Apex Exchange Engine is a production-inspired, low latency, high throughput stock exchange matching engine built in Java.

The goal of this project is NOT to build another CRUD application. Instead, the focus is on learning and demonstrating the backend engineering concepts used inside modern exchanges such as NASDAQ, NYSE, Zerodha, Binance and other high-performance trading systems.

The project emphasizes:

- Low latency
- High throughput
- Concurrent processing
- Event-driven architecture
- Lock minimization
- Horizontal scalability
- Clean software architecture
- Production-grade engineering practices

This repository is being developed incrementally with each phase building toward a realistic distributed exchange architecture.

---

# Current Tech Stack

Backend

- Java 17
- Spring Boot
- Maven
- Apache Kafka
- Spring Kafka
- Micrometer
- Spring Boot Actuator
- Docker Compose

Java Concurrency

- ConcurrentHashMap
- BlockingQueue
- Dedicated worker threads
- Producer / Consumer pattern

Future Technologies

- Redis
- WebSockets
- Prometheus
- Grafana
- Kubernetes
- Cassandra / Event Store (optional)
- Docker Swarm / Kubernetes

---

# Current Project Status

Implemented

- REST Order API
- Kafka Producer
- Kafka Consumer
- In-memory Matching Engine
- OrderBook
- Price-Time Priority Matching
- Buy/Sell Order Matching
- Trade Generation
- Per-Symbol Matching Engine
- Symbol-level concurrency
- One processing thread per trading symbol
- Load Test Runner
- Micrometer metrics
- Spring Actuator metrics
- Graceful shutdown framework

Not Yet Implemented

- Trade streaming
- WebSockets
- Order cancellation
- Order modification
- Market orders
- IOC/FOK orders
- Persistence
- Snapshot recovery
- Horizontal scaling
- Kafka partition strategy
- Docker deployment
- Monitoring dashboards

---

# High Level Architecture

Current Request Flow

HTTP Request

↓

OrderController

↓

OrderProducer

↓

Kafka Topic (orders)

↓

OrderConsumer

↓

MatchingEngineManager

↓

SymbolEngine

↓

MatchingEngine

↓

Trade(s)

↓

TradeProducer

↓

Kafka Topic (trades)

Future

↓

WebSocket Gateway

↓

Frontend

---

# Core Components

## OrderController

Responsibilities

- Accept incoming REST requests
- Validate request
- Publish order to Kafka

Contains NO business logic.

---

## OrderProducer

Responsibilities

- Publish OrderEvent to Kafka

Acts as the entry point into the asynchronous pipeline.

---

## OrderConsumer

Responsibilities

- Consume OrderEvent
- Convert OrderEvent → Order
- Submit order to MatchingEngineManager

---

## MatchingEngineManager

Responsibilities

Maintain one SymbolEngine instance per trading symbol.

Example

AAPL → SymbolEngine

GOOG → SymbolEngine

MSFT → SymbolEngine

Uses ConcurrentHashMap<String, SymbolEngine>.

Creates SymbolEngine lazily using computeIfAbsent().

Guarantees:

One SymbolEngine per symbol.

---

## SymbolEngine

Represents one trading symbol.

Owns

- Symbol name
- MatchingEngine
- BlockingQueue<Order>
- Dedicated processing thread

Responsibilities

Receive orders.

Queue them.

Process them sequentially.

Guarantees

Orders for one symbol are always executed in order.

No locking required inside MatchingEngine.

---

## MatchingEngine

Contains ALL business logic.

Responsibilities

- Match Buy orders
- Match Sell orders
- Generate Trade objects
- Maintain Price-Time Priority

Contains NO Spring Boot code.

Contains NO Kafka code.

Contains NO HTTP code.

Pure business logic.

---

## OrderBook

Maintains

Buy Orders

Max Heap

Highest price first

FIFO for same price

Sell Orders

Min Heap

Lowest price first

FIFO for same price

Supports O(log n) insertion/removal.

---

## TradeProducer

Publishes executed trades to Kafka.

(Current implementation may still be under development.)

---

## TradeConsumer

Consumes Trade events.

(Current implementation may still be under development.)

---

## LoadTestRunner

Generates synthetic traffic.

Purpose

- Stress test engine
- Measure throughput
- Generate thousands of orders automatically

Not intended for production.

---

## EngineShutdownManager

Handles graceful application shutdown.

Future responsibility

Persist snapshots before application exits.

---

# Architectural Decisions

## Why Kafka?

Kafka decouples order submission from order processing.

Benefits

- Asynchronous processing
- High throughput
- Backpressure
- Scalability
- Replay capability

---

## Why One Thread Per Symbol?

Orders for the same symbol MUST execute sequentially.

Instead of locking

Many threads

↓

One OrderBook

↓

Synchronization

↓

Contention

we use

One Symbol

↓

One Queue

↓

One Thread

↓

No contention

Different symbols execute in parallel.

This provides symbol-level concurrency.

---

## Why No Database?

Database writes introduce latency.

Matching engines must operate completely in memory.

Persistence will be added later using

- Snapshots
- Event replay
- Kafka log

This mirrors real exchange architecture.

---

## Why MatchingEngine is NOT a Spring Bean

Business logic should remain framework-independent.

MatchingEngine can therefore

- be unit tested
- be benchmarked
- be reused

without Spring Boot.

---

# Performance Goals

Target Throughput

100,000+ Orders / second

Target Latency

P99 < 1 ms

Characteristics

- Lock-free matching path
- One thread per symbol
- O(log n) order insertion
- Event-driven architecture
- High cache locality

---

# Development Principles

Whenever modifying this project:

DO

- Keep business logic inside MatchingEngine.
- Preserve symbol-level concurrency.
- Keep MatchingEngine independent of Spring.
- Prefer immutable models.
- Explain architectural changes.
- Keep methods small.
- Preserve thread safety.
- Ensure project builds successfully after every change.

DO NOT

- Convert project into CRUD architecture.
- Add unnecessary database calls.
- Introduce global locks.
- Mix transport logic with business logic.
- Break existing architecture without explanation.

---

# AI Assistant Guidelines

Before making code changes:

1. Inspect the current implementation.
2. Do NOT assume constructor signatures.
3. Update all dependent classes together.
4. Explain why every change is required.
5. Ensure Maven build succeeds.
6. Preserve existing functionality.
7. Follow the architecture defined in this document.

---

# Long Term Roadmap

Phase 1

✓ Matching Engine

Phase 2

✓ Kafka Integration

Phase 3

✓ Per Symbol Matching Engine

Phase 4

✓ Symbol-Level Concurrency

Phase 5

⬜ Trade Streaming

Phase 6

⬜ WebSocket Market Data

Phase 7

⬜ Snapshot & Recovery

Phase 8

⬜ Horizontal Scaling

Phase 9

⬜ Distributed Exchange Deployment

---

# Repository Structure

backend/

src/

controller/

REST APIs

kafka/

Kafka Producers & Consumers

core/

OrderBook

model/

Entities & Events

service/

Business logic

load/

Benchmarking utilities

resources/

application.yml

---

# Project Vision

The final system should resemble a simplified production stock exchange capable of handling large order volumes with low latency while demonstrating modern backend engineering practices including:

- Concurrent programming
- Event-driven architecture
- Kafka
- WebSockets
- Horizontal scalability
- Snapshot recovery
- Monitoring
- Containerization
- Distributed systems

The project is intended to serve as a flagship backend engineering portfolio project for software engineering interviews at top product companies.