---
name: perf-benchmark
description: Trigger on-demand multithreaded order matching benchmarks and analyze throughput and latency percentiles via REST and Actuator metrics.
---

# Performance Benchmark Skill Runbook

This skill outlines how to trigger the multithreaded benchmark suite against a running exchange engine instance, evaluate throughput (ops/sec), and inspect Micrometer latency percentiles (p50, p95, p99).

## Prerequisites

1. The backend application must be running on `http://localhost:8080`.
   ```powershell
   cd backend
   .\mvnw.cmd spring-boot:run
   ```

## Benchmark Execution

### 1. Trigger the REST Benchmark Endpoint
Submit an HTTP POST request to [`LoadTestController`](file:///c:/Users/satyv/Exchange-Engine/Apex-Exchange-Engine/backend/src/main/java/com/apex/exchange/load/LoadTestController.java):

```powershell
# Default: 8 threads, 10,000 orders/thread (80,000 total orders)
curl.exe -X POST "http://localhost:8080/load-test/run?threads=8&ordersPerThread=10000"
```

**Expected JSON Response**:
```json
{
  "totalOrders": 80000,
  "threads": 8,
  "durationSeconds": 1.45,
  "throughputOpsPerSec": 55172.41,
  "avgLatencyUs": 18.12,
  "timestamp": 1727700000000
}
```

### 2. Query the Most Recent Benchmark Result
Retrieve the cached result of the last execution:

```powershell
curl.exe "http://localhost:8080/load-test/results"
```

### 3. Inspect Micrometer Latency Percentiles
Inspect engine latency metrics recorded by [`EngineLatencyMetrics`](file:///c:/Users/satyv/Exchange-Engine/Apex-Exchange-Engine/backend/src/main/java/com/apex/exchange/engine/metrics/EngineLatencyMetrics.java) via Spring Boot Actuator:

```powershell
# Order end-to-end latency percentiles (p50, p95, p99)
curl.exe "http://localhost:8080/actuator/metrics/matching.engine.order.latency"

# Pure symbol engine processing time
curl.exe "http://localhost:8080/actuator/metrics/matching.engine.symbol.processing.time"

# Dynamic throughput gauge
curl.exe "http://localhost:8080/actuator/metrics/loadtest.throughput.ops_per_sec"
```

## Performance Acceptance Criteria

* **Throughput**: Should exceed **50,000 ops/sec** under warm JVM conditions.
* **Average Latency**: Should remain under **25µs** per order.
* **p99 Latency**: Should remain strictly below **500µs** (`0.5ms`).
