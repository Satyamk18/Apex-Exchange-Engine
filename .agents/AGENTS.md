# Project-Scoped Rules for Apex Exchange Engine

You are assisting a developer working on the **Apex Exchange Engine** codebase. To ensure architectural integrity, consistency, and alignment with past decisions, you must follow these guidelines:

## 1. Core Documentation Reference
Before proposing code changes or beginning tasks, locate and read the following documents in the workspace:
- **[PROJECT_CONTEXT.md](file:///c:/Users/satyv/Exchange-Engine/Apex-Exchange-Engine/PROJECT_CONTEXT.md)**: Details the overall architecture, tech stack, request flow, and core service definitions.
- **[DEV_SETUP.md](file:///c:/Users/satyv/Exchange-Engine/Apex-Exchange-Engine/DEV_SETUP.md)**: Explains the local setup, Docker dependencies, REST API list, WebSocket STOMP subscription paths, and testing instructions.
- **[FUTURE_ROADMAP.md](file:///c:/Users/satyv/Exchange-Engine/Apex-Exchange-Engine/FUTURE_ROADMAP.md)**: Documents future architecture phases (low-latency zero-allocation collections, pre-trade risk engine, clustered horizontal scaling, etc.).

## 2. Architectural Guidelines
- **Domain Purity**: Ensure the matching logic in [MatchingEngine.java](file:///c:/Users/satyv/Exchange-Engine/Apex-Exchange-Engine/backend/src/main/java/com/apex/exchange/engine/service/MatchingEngine.java) and [OrderBook.java](file:///c:/Users/satyv/Exchange-Engine/Apex-Exchange-Engine/backend/src/main/java/com/apex/exchange/engine/core/OrderBook.java) remains independent of Spring, JPA, or Kafka framework dependencies.
- **Single-Threaded Partitioning**: Respect the symbol-partitioned concurrency model. State modification for a specific symbol (bids, asks, cancellations) must always execute sequentially inside its respective [SymbolEngine.java](file:///c:/Users/satyv/Exchange-Engine/Apex-Exchange-Engine/backend/src/main/java/com/apex/exchange/engine/service/SymbolEngine.java) worker thread. Do not add global synchronized blocks or locks across different symbols.
- **Asynchronous Persistence**: Persist orders and trades asynchronously using Kafka event consumers ([TradeConsumer.java](file:///c:/Users/satyv/Exchange-Engine/Apex-Exchange-Engine/backend/src/main/java/com/apex/exchange/engine/kafka/TradeConsumer.java)) and JPA, keeping persistence logic off the hot path of the matching engines.
- **Automated Snapshots & Recovery**: Respect the state recovery pattern on startup using JSON files located in the `.snapshots/` directory.

## 3. Best Practices & AI Development Guidelines
When writing or modifying code in this workspace, adhere to the following principles:
- **Zero-Allocation Mindset**: Avoid creating short-lived wrapper objects on the hot processing path where possible. When implementing improvements, optimize collection types and object reuse.
- **Thread Safety**: Never assume multi-threaded access is safe. All global state or trackers (like [OrderStatusTracker.java](file:///c:/Users/satyv/Exchange-Engine/Apex-Exchange-Engine/backend/src/main/java/com/apex/exchange/engine/service/OrderStatusTracker.java)) must use concurrency-safe primitives, concurrent maps, or synchronized wrappers.
- **Incremental & Precise Changes**: Make precise edits. Use localized tool replacement chunks instead of rewriting entire classes.
- **Code Completeness**: Do not introduce incomplete "placeholder" methods, empty `TODO` tags, or mock functions. All proposed code must be fully functional and compile immediately.
- **Structured Logging**: Use `slf4j` Logger (`log.info`, `log.debug`, `log.error`) for all logging needs. Never use standard print streams (`System.out.println`).
- **Defensive Error Handling**: Wrap message consumption and execution loops in robust try-catch blocks to prevent worker thread crashes (e.g. within [SymbolEngine.java](file:///c:/Users/satyv/Exchange-Engine/Apex-Exchange-Engine/backend/src/main/java/com/apex/exchange/engine/service/SymbolEngine.java) command loops).
- **Test Integrity**: Every feature update must be followed by running the unit test suite (`./mvnw test`). If new logic is introduced, accompanying unit or integration tests must be written to verify its correctness.
- **Maintain Documentation**: If any code change alters API endpoints, WebSocket channels, persistence models, or configuration keys, immediately update [PROJECT_CONTEXT.md](file:///c:/Users/satyv/Exchange-Engine/Apex-Exchange-Engine/PROJECT_CONTEXT.md) and [DEV_SETUP.md](file:///c:/Users/satyv/Exchange-Engine/Apex-Exchange-Engine/DEV_SETUP.md).

## 4. Communication Style
- Keep responses concise, precise, and professional.
- Refer directly to classes and configuration files using markdown links matching the workspace directory.
