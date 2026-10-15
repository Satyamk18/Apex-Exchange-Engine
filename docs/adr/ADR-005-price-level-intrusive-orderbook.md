# ADR-005: Intrusive Doubly-Linked PriceLevel OrderBook & Hot-Path Optimizations

## Status
Accepted

## Context
Under high-throughput order flow, the previous order book architecture had two severe algorithmic bottlenecks on the critical execution path:
1. **$O(N)$ Order Cancellations**: `OrderBook` stored orders in standard Java `PriorityQueue` collections. Invoking `PriorityQueue.remove(order)` triggered a linear array scan across all resting orders ($O(N)$). In electronic trading where 60–90% of order volume consists of cancellations, this caused severe tail-latency spikes.
2. **$O(B \log B)$ Full-Book Depth Scans & Synchronous Broadcasts**: On every order matched, `SymbolEngine` dumped the entire `PriorityQueue` into a `TreeMap<Double, PriceLevelDto>`, sorted and boxed all prices, and synchronously invoked Spring STOMP's `convertAndSend()` on the matching worker thread.
3. **Synchronous Disk Snapshotting**: The matching thread paused order processing every 1,000 operations to serialize JSON and execute synchronous disk I/O.
4. **OS Entropy Locks in Trade IDs**: `UUID.randomUUID()` acquired synchronized locks on the OS entropy pool via `SecureRandom`.

## Decision
We refactored the matching core and hot path to achieve microsecond-level determinism:
1. **Intrusive `PriceLevel` Doubly-Linked List**:
   - Implemented [`PriceLevel`](file:///c:/Users/satyv/Exchange-Engine/Apex-Exchange-Engine/backend/src/main/java/com/apex/exchange/engine/core/PriceLevel.java) maintaining FIFO `head` and `tail` pointers.
   - Added intrusive pointers (`prev`, `next`, `parentLevel`) directly on [`Order`](file:///c:/Users/satyv/Exchange-Engine/Apex-Exchange-Engine/backend/src/main/java/com/apex/exchange/engine/model/Order.java) (annotated with `@JsonIgnore`).
   - Combined with a secondary index `Map<String, Order> activeOrders`, cancellations unlink the node directly in **$O(1)$** without traversing any collections.
2. **Precomputed $O(\text{depth})$ Market Data & Conflation**:
   - `PriceLevel` maintains incremental `totalQuantity` and `orderCount`.
   - `OrderBook.getDepth()` directly reads the top $N$ price levels in $O(\text{depth})$ without scanning the rest of the book.
   - Depth updates are conflated: dispatched only on Disruptor batch completion (`endOfBatch == true`) and throttled to a minimum interval (50ms).
3. **Asynchronous Non-Blocking Snapshot Persistence**:
   - The symbol matching thread takes an in-memory structural snapshot in microseconds.
   - Serialization and atomic disk writes (`.tmp` $\to$ `.snapshot.json`) are offloaded to an asynchronous background worker pool in [`SnapshotService`](file:///c:/Users/satyv/Exchange-Engine/Apex-Exchange-Engine/backend/src/main/java/com/apex/exchange/engine/snapshot/SnapshotService.java).
4. **Deterministic Monotonic Trade IDs**:
   - Replaced `UUID.randomUUID()` with per-symbol monotonic sequence generators (`{symbol}-T{tradeSeq}`) with zero lock contention.
5. **Bounded Terminal Order Tracking**:
   - [`OrderStatusTracker`](file:///c:/Users/satyv/Exchange-Engine/Apex-Exchange-Engine/backend/src/main/java/com/apex/exchange/engine/service/OrderStatusTracker.java) implements bounded eviction for terminal orders (`FILLED`/`CANCELLED`), preventing JVM heap exhaustion.

## Consequences

### Positive
* **Instantaneous $O(1)$ Cancellations**: Order cancellations no longer degrade as resting book depth increases.
* **Unblocked Matching Thread**: Conflated WebSocket broadcasting and asynchronous snapshot disk writes eliminate multi-millisecond stalls on the hot path.
* **Zero Entropy Contention**: Deterministic trade IDs eliminate OS entropy lock contention and make trade executions 100% idempotent on event replay.
* **Bounded Heap Footprint**: Terminal order memory is strictly bounded, preventing `OutOfMemoryError` under sustained millions of orders.

### Negative / Trade-offs
* Memory overhead per resting order increases slightly due to `prev`, `next`, and `parentLevel` references (3 references = 24 bytes on 64-bit JVMs with compressed OOPs).
