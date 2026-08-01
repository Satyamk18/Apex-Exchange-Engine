# PROJECT_CONTEXT.md

# Apex Exchange Engine

## Project Vision

Apex Exchange Engine is a production-inspired low-latency stock exchange matching engine built to simulate how modern electronic trading systems operate.

The objective is not merely to build a working application, but to design and implement a system that demonstrates production-grade backend engineering principles including scalability, concurrency, maintainability, and clean architecture.

Every architectural decision should prioritize long-term maintainability, correctness, and performance over short-term implementation convenience.

---

# Goals

The project aims to:

- Build a deterministic order matching engine
- Handle concurrent order ingestion safely
- Process trades with low latency
- Demonstrate event-driven architecture
- Scale horizontally by symbol
- Showcase production-quality engineering practices
- Serve as a portfolio project that reflects real-world backend system design

---

# Non Goals

This project intentionally does not aim to replicate an entire stock exchange.

The following are currently out of scope:

- Market data dissemination
- FIX protocol
- Authentication and authorization
- User management
- Risk management
- Regulatory compliance
- Trading UI

These may be explored in future iterations.

---

# Current Tech Stack

Language
- Java 17

Backend
- Spring Boot

Messaging
- Apache Kafka

Build Tool
- Maven

Infrastructure
- Docker Compose

Monitoring
- Spring Actuator
- Micrometer

---

# Current Architecture

Current request flow:

Client

↓

REST Controller

↓

Kafka Producer

↓

Kafka Topic

↓

Kafka Consumer

↓

MatchingEngineManager

↓

SymbolEngine

↓

MatchingEngine

↓

Trade Event

↓

Kafka Trade Topic

The architecture is intentionally event-driven to separate request handling from business processing.

---

# Core Components

## OrderController

Receives client requests.

Responsible only for validation and publishing orders.

Contains no business logic.

---

## OrderProducer

Publishes validated orders to Kafka.

---

## OrderConsumer

Consumes orders from Kafka.

Routes each order to the appropriate SymbolEngine.

---

## MatchingEngineManager

Maintains a registry of SymbolEngines.

Ensures one matching engine exists per trading symbol.

---

## SymbolEngine

Owns:

- MatchingEngine
- BlockingQueue
- Dedicated processing thread

Guarantees sequential processing for a single symbol while allowing parallel processing across multiple symbols.

---

## MatchingEngine

Contains all order matching logic.

Responsible for:

- Buy book
- Sell book
- Price-time priority
- Trade generation

Should remain independent of Spring Boot and Kafka.

---

# Current Concurrency Model

Concurrency is partitioned by trading symbol.

Each symbol owns:

- Dedicated worker thread
- Dedicated queue
- Dedicated matching engine

This eliminates synchronization inside the matching engine while allowing multiple symbols to execute simultaneously.

Future improvements may include more advanced scheduling or partition assignment strategies.

---

# Architectural Principles

The project follows these architectural principles:

- Separation of Concerns
- Single Responsibility Principle
- Composition over Inheritance
- Dependency Injection
- Framework-independent business logic
- High cohesion
- Low coupling

The domain model should never depend directly on infrastructure concerns.

---

# Engineering Expectations

Every implementation should optimize for:

- Correctness
- Readability
- Maintainability
- Testability
- Reliability
- Scalability
- Performance
- Extensibility

Working code is not sufficient.

Solutions should be evaluated from an engineering perspective.

---

# Performance Philosophy

Performance should be achieved through good design rather than premature optimization.

When multiple implementations exist, prefer solutions that:

- Reduce unnecessary allocations
- Minimize lock contention
- Reduce algorithmic complexity
- Improve throughput
- Maintain deterministic behavior

Every optimization should preserve readability and correctness.

---

# Concurrency Philosophy

Concurrency should remain explicit and easy to reason about.

Prefer:

- Message passing
- Immutable objects where practical
- Dedicated ownership of mutable state

Avoid:

- Shared mutable state
- Global synchronization
- Hidden concurrency

Thread safety should be maintained by design rather than by excessive locking.

---

# Engineering Mindset

Think like a backend engineer building software that may eventually process millions of events.

Every design decision should consider:

- Can this scale?

- Is this maintainable?

- Can another engineer understand this?

- What happens during failure?

- Can this component evolve independently?

Trade-offs should always be explained before implementation.

---

# AI Collaboration Guidelines

Before implementing any feature:

1. Understand the current architecture.

2. Explain the proposed design.

3. Identify affected components.

4. Explain trade-offs.

5. Preserve architectural consistency.

After implementation:

- Review the code.
- Check thread safety.
- Look for performance improvements.
- Identify edge cases.
- Suggest possible refactorings.

Do not introduce unnecessary abstractions.

Do not over-engineer.

Prefer incremental improvements over large rewrites.

---

# Code Review Checklist

Every completed feature should satisfy the following:

✓ Correctness

✓ Readability

✓ Maintainability

✓ Thread Safety

✓ Performance

✓ No duplicate logic

✓ Appropriate abstractions

✓ Meaningful naming

✓ Error handling

✓ Logging where appropriate

✓ Build passes

---

# Current Roadmap

Phase 1
- Order matching
- Buy/Sell books
- Market orders
- Limit orders

Phase 2
- Kafka integration
- Symbol partitioning
- Concurrent processing

Phase 3
- Trade publishing
- Trade history
- Order cancellation

Phase 4 (Completed)
- Snapshots
- Recovery
- Persistence

Phase 5
- WebSocket streaming
- Market data
- Horizontal scaling

Phase 6
- Distributed deployment
- Performance benchmarking
- Production hardening

---

# Future Vision

The long-term objective is to evolve Apex Exchange Engine into a production-inspired distributed trading platform that demonstrates modern backend engineering practices, distributed systems design, and low-latency architecture.

The project should emphasize engineering quality over feature quantity and remain a reference implementation for scalable event-driven backend systems.