# Local Development & Setup Guide

Welcome to the **Apex Exchange Engine** developer setup guide. This document provides step-by-step instructions on how to set up, build, run, test, and interact with the exchange engine on your local development machine.

---

## 📋 Prerequisites

Before starting, ensure your local development environment has the following installed:

| Tool | Recommended Version | Purpose |
| :--- | :--- | :--- |
| **Java JDK** | Java 17 or higher | Application runtime & compilation |
| **Docker & Docker Compose** | Docker Desktop v20+ / Docker Compose v2+ | Running infrastructure services (Kafka, Zookeeper, Redis) |
| **Git** | 2.x+ | Version control |
| **Maven Wrapper** | *Included in `./backend`* (`mvnw` / `mvnw.cmd`) | Building, testing, and running the application |

### Recommended IDE
- **IntelliJ IDEA** (Community or Ultimate edition) or **VS Code** with the *Extension Pack for Java*.

---

## 🚀 Step-by-Step Local Setup

### 1. Clone the Repository

Clone the project repository to your local machine and navigate into the workspace directory:

```bash
git clone https://github.com/Satyamk18/Apex-Exchange-Engine.git
cd Apex-Exchange-Engine
```

---

### 2. Start Infrastructure Services via Docker

The project uses Docker Compose to run Apache Kafka, Zookeeper, and Redis.

Start the containers in detached mode:

```bash
docker-compose up -d
```

Verify that all services are running and healthy:

```bash
docker-compose ps
```

**Expected Ports:**
- **Kafka**: `localhost:9092`
- **Zookeeper**: `localhost:2181`
- **Redis**: `localhost:6379`

> [!NOTE]
> On application startup, Kafka topics (`orders` and `trades` with 12 partitions each) will be automatically created by `KafkaTopicConfig.java`.

---

### 3. Build & Run the Backend Application

Navigate into the `backend/` directory:

```bash
cd backend
```

Run the Spring Boot application using the bundled Maven wrapper:

#### On Windows (PowerShell / Command Prompt):
```powershell
.\mvnw.cmd spring-boot:run
```

#### On macOS / Linux:
```bash
chmod +x mvnw  # Ensure wrapper script is executable
./mvnw spring-boot:run
```

The application will start on **`http://localhost:8080`**.

During startup, `EngineRecoveryService` will scan `.snapshots/` for any existing order book state to restore buy/sell order books before accepting live order traffic.

---

### 4. Running Unit & Integration Tests

Execute the full test suite (covering order matching, cancellations, L2 book depth, snapshots, persistence, and WebSocket feeds):

#### Windows:
```powershell
.\mvnw.cmd clean test
```

#### macOS / Linux:
```bash
./mvnw clean test
```

---

### 5. Running the Multithreaded Load Test Benchmark

To execute the multithreaded performance benchmark tool:

#### Windows:
```powershell
.\mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=load-test
```

#### macOS / Linux:
```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=load-test
```

This profile executes `LoadTestRunner`, spawning concurrent order generator threads to benchmark order throughput (orders/sec).

---

## 📡 Interacting with the Exchange Engine

### 📥 1. REST API Endpoints

#### **Submit a New Order**
- **Endpoint**: `POST http://localhost:8080/orders`
- **Headers**: `Content-Type: application/json`
- **Request Body (Limit Buy Order)**:
  ```json
  {
    "orderId": "order-101",
    "symbol": "AAPL",
    "side": "BUY",
    "type": "LIMIT",
    "price": 150.50,
    "quantity": 100
  }
  ```
- **Request Body (Market Sell Order)**:
  ```json
  {
    "orderId": "order-102",
    "symbol": "AAPL",
    "side": "SELL",
    "type": "MARKET",
    "price": 0.0,
    "quantity": 50
  }
  ```

#### **Cancel an Order**
- **Endpoint**: `DELETE http://localhost:8080/orders/{symbol}/{orderId}`
- **Example**: `DELETE http://localhost:8080/orders/AAPL/order-101`

#### **Get Live Order Status**
- **Endpoint**: `GET http://localhost:8080/orders/{orderId}`

#### **Get Level 2 Order Book Depth**
- **Endpoint**: `GET http://localhost:8080/orders/book/{symbol}?depth=10`
- **Example**: `GET http://localhost:8080/orders/book/AAPL?depth=5`

#### **Query Historical Data**
- Historical trades for symbol: `GET http://localhost:8080/history/trades/AAPL`
- Historical trades for order ID: `GET http://localhost:8080/history/trades/order/order-101`
- Historical orders for symbol: `GET http://localhost:8080/history/orders/AAPL`

---

### 🔌 2. WebSocket Real-Time Feeds (STOMP)

Connect any STOMP-compatible WebSocket client (e.g. Postman WebSocket request or SockJS/stompjs in a Web UI).

- **STOMP Connection Endpoint**: `ws://localhost:8080/ws-exchange` (or `http://localhost:8080/ws-exchange` for SockJS fallback)
- **Subscribed Channels**:
  - `/topic/depth/{symbol}` — Real-time L2 order book top-N bids & asks depth updates
  - `/topic/trades/{symbol}` — Real-time trade execution notifications
  - `/topic/ticker/{symbol}` — Real-time 24h rolling ticker statistics (last price, high, low, total volume)

---

### 🗄️ 3. Database Console (H2 Console)

You can view persisted orders and executed trades using the built-in web console.

- **URL**: `http://localhost:8080/h2-console`
- **Driver Class**: `org.h2.Driver`
- **JDBC URL**: `jdbc:h2:file:./data/apexdb;AUTO_SERVER=TRUE`
- **User Name**: `sa`
- **Password**: *(leave empty)*

---

### 🩺 4. Health & Metrics (Spring Actuator)

- **Health Status**: `GET http://localhost:8080/actuator/health`
- **Available Metrics**: `GET http://localhost:8080/actuator/metrics`

---

## 📁 Local Data Directories

During execution, the application creates two local directories under `backend/`:

1. `backend/data/`: Stores the H2 relational database file (`apexdb.mv.db`) for persisted orders and trades.
2. `backend/.snapshots/`: Stores JSON snapshot files (`{symbol}.snapshot.json`) representing active order books for recovery.

> [!TIP]
> To perform a complete fresh reset of the local state, stop the server and remove the `./data/` and `.snapshots/` directories:
> ```bash
> rm -rf backend/data backend/.snapshots
> ```

---

## ❓ Troubleshooting

### 1. Connection Refused to Kafka (`localhost:9092`)
- Ensure Docker Desktop is running.
- Verify containers with `docker-compose ps`.
- Restart infrastructure: `docker-compose down && docker-compose up -d`.

### 2. Port Conflict Errors (`8080` or `9092`)
- Verify no other process is bound to port `8080` (Spring Boot) or `9092` (Kafka).
- On Windows: `netstat -ano | findstr 8080`
- On macOS/Linux: `lsof -i :8080`

### 3. Permission Denied for `./mvnw`
- Run `chmod +x backend/mvnw` to grant execution permissions to the Maven wrapper.

---

## 🤝 Project Structure Quick Reference

```text
Apex-Exchange-Engine/
├── backend/
│   ├── src/main/java/com/apex/exchange/
│   │   ├── engine/
│   │   │   ├── config/          # WebSocket & Application Config
│   │   │   ├── controller/      # REST API Controllers (OrderController, HistoryController)
│   │   │   ├── core/            # Core OrderBook domain model
│   │   │   ├── kafka/           # Order/Trade Producers & Consumers
│   │   │   ├── model/           # DTOs, Enums, Orders & Trades
│   │   │   ├── persistence/     # JPA Entities, Repositories, Persistence Service
│   │   │   ├── service/         # MatchingEngine, SymbolEngine, MatchingEngineManager
│   │   │   ├── snapshot/        # SnapshotService & EngineRecoveryService
│   │   │   └── websocket/       # MarketDataBroadcaster (STOMP WebSocket broker)
│   │   └── load/                # Multithreaded LoadTestRunner
│   ├── src/test/java/           # Unit & Integration Test suite
│   ├── mvnw & mvnw.cmd          # Maven Wrapper scripts
│   └── pom.xml                  # Maven Project dependencies
├── docker-compose.yml           # Docker services (Kafka, Zookeeper, Redis)
├── PROJECT_CONTEXT.md           # Architecture & Project Roadmap Context
└── DEV_SETUP.md                 # Local Development Setup Guide (This file)
```
