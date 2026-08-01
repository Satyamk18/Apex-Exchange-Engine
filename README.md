# Apex Exchange Engine

A high-performance, distributed, event-driven stock exchange matching engine built with **Java 17**, **Spring Boot**, **Apache Kafka**, **Spring Data JPA**, and **H2/PostgreSQL**. 

Apex Exchange Engine simulates core electronic trading operations—including Price-Time Priority order matching, multi-partition event streaming, asynchronous event persistence, periodic state snapshotting, and crash recovery.

---

## 🌟 Key Features

* **Deterministic Order Matching Engine**: Supports **LIMIT** and **MARKET** orders with strict **Price-Time Priority** matching algorithms.
* **Symbol-Partitioned Concurrency**: Single-threaded execution per symbol via thread-safe `BlockingQueue` and `SymbolEngine` loops, eliminating lock contention on the hot path while processing separate symbols in parallel.
* **Event-Driven Architecture with Kafka**: Scalable order ingestion and trade execution broadcast via partition-aware Apache Kafka topics.
* **Order Cancellations & Level 2 Depth Aggregation**: Support for on-the-fly order cancellation and real-time top-N order book depth queries (bids & asks).
* **Non-Blocking Persistence**: Async DB logging via Spring `@Async` and Spring Data JPA to write trades and order lifecycle states into an H2 database off the critical path.
* **Snapshotting & Crash Recovery**: Periodic JSON order book snapshots stored on disk (`.snapshots/`), with automated state restoration on application startup (`EngineRecoveryService`).
* **REST Query & History APIs**: Query live order statuses, top-N order book depth, and historical trade/order records via REST endpoints.

---

## 🏗️ System Architecture

```
                                    +-----------------------+
                                    |    REST Controller    |
                                    +-----------+-----------+
                                                |
                                                v
                                    +-----------------------+
                                    |  Kafka Order Topic    |
                                    |    (12 Partitions)    |
                                    +-----------+-----------+
                                                |
                                                v
                                  +-------------+-------------+
                                  |   MatchingEngineManager   |
                                  +-------------+-------------+
                                                |
                        +-----------------------+-----------------------+
                        | (Partitioned by Symbol)                        |
                        v                                               v
              +-------------------+                           +-------------------+
              |   SymbolEngine    |                           |   SymbolEngine    |
              |     (AAPL)        |                           |     (TSLA)        |
              +---------+---------+                           +---------+---------+
                        |                                               |
                        +-----------------------+-----------------------+
                                                |
                                                v
                                    +-----------+-----------+
                                    |   MatchingEngine      |
                                    |  (Price-Time Match)   |
                                    +-----------+-----------+
                                        /       |       \
                                       /        |        \
                                      v         v         v
                    +-------------------+  +----------+  +----------------------+
                    | Kafka Trade Topic |  | DB Async |  | Periodic Snapshots   |
                    | (Broadcast Trades)|  | (H2 JPA) |  | (.snapshots/*.json)  |
                    +-------------------+  +----------+  +----------------------+
```

---

## 🛠️ Technology Stack

* **Language**: Java 17
* **Framework**: Spring Boot 4.x / Spring Data JPA
* **Messaging**: Apache Kafka (12 Partitions)
* **Database**: H2 Database (File & In-Memory Mode) / PostgreSQL-ready
* **Build System**: Maven
* **Documentation & Metrics**: OpenAPI / Swagger UI, Spring Actuator, Micrometer

---

## 🚀 Getting Started

### Prerequisites

* **Java 17 JDK** or higher
* **Apache Kafka** running on `localhost:9092` (or via Docker Compose)
* **Maven 3.8+** (or use the bundled `mvnw`)

### 1. Start Infrastructure via Docker Compose
```bash
docker-compose up -d
```

### 2. Build and Run the Exchange Engine
```bash
cd backend
./mvnw clean spring-boot:run
```

### 3. Run the Test Suite
```bash
cd backend
./mvnw clean test
```

---

## 📡 API Reference

### 📥 Order Ingestion & Lifecycle
* `POST /orders` — Submit a new order (Limit or Market)
* `DELETE /orders/{orderId}` — Cancel an active open order
* `GET /orders/{orderId}/status` — Fetch live order status (NEW, PARTIALLY_FILLED, FILLED, CANCELLED)

### 📊 Market Data & Depth
* `GET /orderbook/depth/{symbol}?depth=10` — Fetch real-time Level 2 order book depth (top N bids & asks)

### 📜 Trade & Order History
* `GET /history/trades/{symbol}` — Retrieve all historical executed trades for a symbol
* `GET /history/trades/order/{orderId}` — Retrieve trades executed for a specific order ID
* `GET /history/orders/{symbol}` — Retrieve historical orders for a symbol
* `GET /h2-console` — H2 Database Console (`jdbc:h2:file:./data/apexdb`)

---

## 📈 Roadmap & Phase Progress

- [x] **Phase 1**: Core Order Matching (Limit/Market Orders, Price-Time Priority, Order Books)
- [x] **Phase 2**: Event Ingestion & Partitioning (Kafka Order Topics, Symbol Partitioning)
- [x] **Phase 3**: Trade Publishing, Order Cancellation & Level 2 Depth Aggregation
- [x] **Phase 4**: Persistence, Periodic Snapshotting & Engine Crash Recovery
- [ ] **Phase 5**: API Gateway, Live WebSocket Streaming & Ticker Feeds *(Up Next)*
- [ ] **Phase 6**: User Wallets, Pre-Trade Balance Locking & High-Throughput Benchmarks
