---
name: load-testing
description: Run, benchmark, and analyze the multithreaded order matching load test suite in Apex Exchange Engine.
---

# Load Testing Skill Runbook

This skill provides step-by-step instructions for launching and evaluating order throughput for [LoadTestRunner.java](file:///c:/Users/satyv/Exchange-Engine/Apex-Exchange-Engine/backend/src/main/java/com/apex/exchange/load/LoadTestRunner.java).

## Prerequisites

1. Ensure the Maven wrapper or Maven CLI is available in `backend/`.
2. Zookeeper/Kafka Docker services (optional depending on whether Kafka persistence profile is enabled).

## Execution Procedure

### 1. Launch the Load Test Profile
Execute the Maven Spring Boot runner with the `load-test` profile enabled:

```powershell
cd backend
./mvnw spring-boot:run "-Dspring-boot.run.profiles=load-test"
```

### 2. Output Analysis & Key Metrics
The load generator submits concurrent orders across multiple worker threads to [MatchingEngineManager.java](file:///c:/Users/satyv/Exchange-Engine/Apex-Exchange-Engine/backend/src/main/java/com/apex/exchange/engine/service/MatchingEngineManager.java).

Monitor for log outputs reporting:
- **Total Processed Orders**: Expected `80,000` (8 threads × 10,000 orders/thread).
- **Execution Duration**: Measured in seconds elapsed.
- **Throughput (Ops/Sec)**: Calculated as $\frac{\text{Total Orders}}{\text{Time in Seconds}}$.

### 3. Saturation & Queue Health Check
If throughput drops significantly or thread deadlocks occur:
- Verify single-threaded partition routing inside [SymbolEngine.java](file:///c:/Users/satyv/Exchange-Engine/Apex-Exchange-Engine/backend/src/main/java/com/apex/exchange/engine/service/SymbolEngine.java).
- Ensure no shared lock contention across symbol partitions.
