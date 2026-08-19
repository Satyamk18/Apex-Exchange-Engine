---
name: build-and-test
description: Build, compile, and execute unit and integration test suites for Apex Exchange Engine.
---

# Build & Test Skill Runbook

Use this skill whenever code modifications are made to ensure zero build errors and verify architectural guidelines (domain purity, single-threaded symbol execution, and test integrity).

## Execution Commands

### 1. Compile Backend Project
From the repository root or `backend/` directory:

```powershell
cd backend
./mvnw clean compile
```

### 2. Run Full Unit & Integration Test Suite
```powershell
cd backend
./mvnw test
```

## Architectural Guidelines Verification Checklist

When building or adding new features:
1. **Domain Purity**: Core matching logic in [MatchingEngine.java](file:///c:/Users/satyv/Exchange-Engine/Apex-Exchange-Engine/backend/src/main/java/com/apex/exchange/engine/service/MatchingEngine.java) and [OrderBook.java](file:///c:/Users/satyv/Exchange-Engine/Apex-Exchange-Engine/backend/src/main/java/com/apex/exchange/engine/core/OrderBook.java) must remain pure Java with zero Spring or Kafka dependencies.
2. **Symbol Thread Safety**: Ensure changes inside [SymbolEngine.java](file:///c:/Users/satyv/Exchange-Engine/Apex-Exchange-Engine/backend/src/main/java/com/apex/exchange/engine/service/SymbolEngine.java) modify state strictly within its assigned single worker thread.
3. **Async Persistence**: Verify trade logging stays decoupled via [TradeConsumer.java](file:///c:/Users/satyv/Exchange-Engine/Apex-Exchange-Engine/backend/src/main/java/com/apex/exchange/engine/kafka/TradeConsumer.java).
