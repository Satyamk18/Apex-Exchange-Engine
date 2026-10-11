# ADR-003: Asynchronous Kafka-Driven Persistence

## Status
Accepted

## Context
When an order matches, trade execution records and updated order statuses must be stored in a relational database (PostgreSQL / H2) for regulatory audit trails, accounting, and user query APIs ([`HistoryController`](file:///c:/Users/satyv/Exchange-Engine/Apex-Exchange-Engine/backend/src/main/java/com/apex/exchange/engine/controller/HistoryController.java)).

However, executing relational database transactions (JDBC/JPA) synchronously within the matching loop takes anywhere from 1 to 20+ milliseconds per transaction due to connection pooling, network hops, and disk I/O. Forcing the matching engine to wait for database confirmation would reduce engine throughput from 50,000+ ops/sec down to mere hundreds of orders per second.

## Decision
We decouple persistence completely from the matching engine hot path using an asynchronous event-driven Kafka pipeline:
1. When [`MatchingEngine`](file:///c:/Users/satyv/Exchange-Engine/Apex-Exchange-Engine/backend/src/main/java/com/apex/exchange/engine/service/MatchingEngine.java) matches orders, [`SymbolEngine`](file:///c:/Users/satyv/Exchange-Engine/Apex-Exchange-Engine/backend/src/main/java/com/apex/exchange/engine/service/SymbolEngine.java) publishes the executed `Trade` objects to the Kafka `trades` topic via [`TradeProducer`](file:///c:/Users/satyv/Exchange-Engine/Apex-Exchange-Engine/backend/src/main/java/com/apex/exchange/engine/kafka/TradeProducer.java).
2. A separate background consumer [`TradeConsumer`](file:///c:/Users/satyv/Exchange-Engine/Apex-Exchange-Engine/backend/src/main/java/com/apex/exchange/engine/kafka/TradeConsumer.java) listens to the `trades` topic.
3. The consumer delegates to [`PersistenceService`](file:///c:/Users/satyv/Exchange-Engine/Apex-Exchange-Engine/backend/src/main/java/com/apex/exchange/engine/persistence/service/PersistenceService.java) to persist `TradeEntity` and `OrderEntity` instances via Spring Data JPA repositories in independent database transactions.

## Consequences

### Positive
* **Zero Disk I/O on Hot Path**: The matching engine operates entirely in RAM and never waits for database transactions or disk commits.
* **Elastic Backpressure**: If database writes slow down under high load, Kafka securely buffers trade messages without slowing down live order matching.
* **Decoupled Architecture**: Persistence failures or database maintenance windows do not immediately crash the core order matching process.

### Negative / Trade-offs
* Eventual consistency: A trade query made via `/history/trades/{symbol}` immediately after order placement might lag behind real-time matching state by a few milliseconds. (Live state is served via in-memory [`OrderStatusTracker`](file:///c:/Users/satyv/Exchange-Engine/Apex-Exchange-Engine/backend/src/main/java/com/apex/exchange/engine/service/OrderStatusTracker.java) and WebSockets).
