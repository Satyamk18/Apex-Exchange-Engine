# ADR-004: Periodic JSON Snapshots & Startup State Recovery

## Status
Accepted

## Context
Because [`MatchingEngine`](file:///c:/Users/satyv/Exchange-Engine/Apex-Exchange-Engine/backend/src/main/java/com/apex/exchange/engine/service/MatchingEngine.java) and [`OrderBook`](file:///c:/Users/satyv/Exchange-Engine/Apex-Exchange-Engine/backend/src/main/java/com/apex/exchange/engine/core/OrderBook.java) operate completely in volatile memory (RAM) for ultra-low latency, a sudden server reboot, power failure, or application restart would cause all resting bids and asks to vanish.

Rebuilding the entire order book state by replaying an entire history of millions of Kafka messages from offset 0 would lead to unacceptably long startup times (minutes or hours).

## Decision
We implement a periodic snapshotting and automated startup hydration pattern:
1. **Periodic Snapshot Trigger**: In [`SymbolEngine`](file:///c:/Users/satyv/Exchange-Engine/Apex-Exchange-Engine/backend/src/main/java/com/apex/exchange/engine/service/SymbolEngine.java), an atomic operation counter tracks processed events. Every `1,000` operations (`SNAPSHOT_INTERVAL`), it triggers [`SnapshotService.saveSnapshot()`](file:///c:/Users/satyv/Exchange-Engine/Apex-Exchange-Engine/backend/src/main/java/com/apex/exchange/engine/snapshot/SnapshotService.java).
2. **File Storage**: Serializes active bids and asks into JSON files stored at `.snapshots/{symbol}.snapshot.json`.
3. **Automated Recovery**: [`EngineRecoveryService`](file:///c:/Users/satyv/Exchange-Engine/Apex-Exchange-Engine/backend/src/main/java/com/apex/exchange/engine/snapshot/EngineRecoveryService.java) listens to Spring's `ApplicationReadyEvent`. Upon startup, it scans `.snapshots/`, restores all resting bids/asks into their respective `MatchingEngine` instances, and hydrates [`OrderStatusTracker`](file:///c:/Users/satyv/Exchange-Engine/Apex-Exchange-Engine/backend/src/main/java/com/apex/exchange/engine/service/OrderStatusTracker.java) before any external traffic is accepted.

## Consequences

### Positive
* **Sub-Second Crash Recovery**: Restores full order book state in milliseconds upon application startup without replaying full historical event logs.
* **Bounded Data Loss Window**: In catastrophic failure scenarios, maximum un-snapshotted operations are capped at 1,000 events (which can be replayed from Kafka offsets).
* **Transparent Verification**: JSON snapshots are human-readable, making auditing and debugging resting order book states straightforward.

### Negative / Trade-offs
* Disk write I/O occurs once every 1,000 operations (mitigated by writing asynchronously or during single-operation handoffs).
* Future phases will evolve local disk storage into distributed blob storage (AWS S3, MinIO) for clustered HA deployments.
