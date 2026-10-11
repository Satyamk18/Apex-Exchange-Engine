# ADR-001: Symbol-Partitioned Concurrency Model

## Status
Accepted

## Context
A high-throughput electronic stock exchange must process incoming orders with deterministic price-time priority without data races. The naive approach to multi-threaded order matching is either:
1. **Global Engine Locking**: A single global lock or mutex around the entire exchange matching engine.
2. **Fine-Grained OrderBook Locking**: Synchronized blocks or ReadWriteLocks inside the order book collections.

Both approaches cause significant lock contention, CPU cache line bouncing, and OS thread context switching under high concurrency. Furthermore, transactions for stock `AAPL` have zero logical dependency on transactions for stock `TSLA`—their order books are completely independent state machines.

## Decision
We partition concurrency strictly by trading **symbol**:
1. Exactly one dedicated [`SymbolEngine`](file:///c:/Users/satyv/Exchange-Engine/Apex-Exchange-Engine/backend/src/main/java/com/apex/exchange/engine/service/SymbolEngine.java) instance exists per symbol, registered in [`MatchingEngineManager`](file:///c:/Users/satyv/Exchange-Engine/Apex-Exchange-Engine/backend/src/main/java/com/apex/exchange/engine/service/MatchingEngineManager.java).
2. Each `SymbolEngine` owns its own dedicated execution thread and input event buffer.
3. Orders for a given symbol are executed strictly sequentially within that symbol's thread, while different symbols execute in parallel across CPU cores.

## Consequences

### Positive
* **Zero Cross-Symbol Lock Contention**: Order matching on `AAPL` never blocks or contends with orders on `TSLA` or `MSFT`.
* **Deterministic Matching**: Sequential execution within a symbol guarantees strict, deterministic Price-Time Priority order execution.
* **Domain Purity**: The core [`MatchingEngine`](file:///c:/Users/satyv/Exchange-Engine/Apex-Exchange-Engine/backend/src/main/java/com/apex/exchange/engine/service/MatchingEngine.java) and [`OrderBook`](file:///c:/Users/satyv/Exchange-Engine/Apex-Exchange-Engine/backend/src/main/java/com/apex/exchange/engine/core/OrderBook.java) can remain pure single-threaded Java without locks, volatile fields, or concurrency primitives.

### Negative / Trade-offs
* Orders for a single heavily traded symbol cannot be parallelized across multiple threads (this is an intentional exchange design principle).
* Cluster horizontal scaling requires routing keys by symbol partition.
