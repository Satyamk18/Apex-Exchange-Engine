**Apex-Exchange-Engine**

Apex Exchange Engine is a distributed real-time trading backend designed to simulate a multi-symbol exchange.
The system implements partition-aware order matching with strict price-time priority, leveraging Apache Kafka for ordered event streaming, Redis for low-latency state snapshots, and WebSockets for live trade dissemination.

**Architecture Overview**

- Spring Boot (Order Ingestion & API Layer)
- Apache Kafka (Event Streaming & Partitioned Processing)
- In-Memory Matching Engine (Per-Symbol Order Books)
- Redis (Order Book Snapshot & Fast Reads)
- WebSocket Streaming (Real-Time Trade Updates)
- Docker (Kafka + Redis Infrastructure)

**Key Features**

- Partition-based concurrency model
- Strict price-time priority matching
- Concurrent multi-symbol processing
- Event-driven trade execution
- Redis-backed state caching
- Real-time order book streaming

**Infrastructure**

Start Kafka and Redis locally:

```bash
docker compose up -d
