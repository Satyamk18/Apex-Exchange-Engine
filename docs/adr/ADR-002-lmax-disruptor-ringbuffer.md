# ADR-002: LMAX Disruptor Ring Buffer for SymbolEngine

## Status
Accepted

## Context
Originally, [`SymbolEngine`](file:///c:/Users/satyv/Exchange-Engine/Apex-Exchange-Engine/backend/src/main/java/com/apex/exchange/engine/service/SymbolEngine.java) used a standard Java `LinkedBlockingQueue` to hand off incoming order events from the Kafka consumer thread to the symbol worker thread.

Under load testing (>50,000 ops/sec), this architecture exhibited two major performance bottlenecks:
1. **Lock & Condition Contention**: `LinkedBlockingQueue` relies on `ReentrantLock` and OS signaling (`Condition.await()` / `signal()`). Under heavy ingestion, producers and consumers fight for lock ownership, causing CPU context switching.
2. **GC Allocation Pressure**: Every item enqueued in a `LinkedBlockingQueue` allocates a new `Node` wrapper object on the heap. At 50,000 ops/sec, this floods the JVM Young Generation, causing frequent Garbage Collection (GC) pauses that degrade p99 and p99.9 latencies.

## Decision
We refactored `SymbolEngine` to use an **LMAX Disruptor Ring Buffer**:
1. **Fixed Ring Size**: Configured to 16,384 (`1024 * 16`) slots—a power of two enabling ultra-fast bitwise masking (`sequence & (size - 1)`) instead of modulo division.
2. **Pre-allocated Event Holders**: The ring buffer pre-allocates reusable [`OrderEventHolder`](file:///c:/Users/satyv/Exchange-Engine/Apex-Exchange-Engine/backend/src/main/java/com/apex/exchange/engine/service/SymbolEngine.java) instances at startup. Ingestion overwrites existing slots, and the consumer clears them (`holder.clear()`) after processing, resulting in **zero garbage collection allocation** on the hot path.
3. **Lock-Free Concurrency**: Uses Compare-And-Swap (CAS) on atomic sequence numbers (`YieldingWaitStrategy`, `ProducerType.MULTI`), eliminating OS-level mutex locks.

## Consequences

### Positive
* **Sub-500µs p99 Latency**: Eliminating mutex locks and context switching allowed order matching to hit sub-500µs p99 latency under load.
* **Zero GC Overhead**: Pre-allocated ring buffer objects eliminate node object churn during high-frequency ingestion.
* **Hardware Cache Sympathy**: Array-backed contiguous memory layouts optimize CPU L1/L2 cache prefetching.

### Negative / Trade-offs
* Ring buffer capacity is bounded (16,384 slots). If downstream matching lags behind extreme burst traffic, producers will yield/spin waiting for buffer clearance.
