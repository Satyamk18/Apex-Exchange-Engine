---
name: smoke-test
description: Execute an end-to-end smoke verification test covering order placement, matching, status lifecycle, and trade history.
---

# End-to-End Smoke Test Skill Runbook

This skill provides an automated sanity-check workflow to verify that all layers of the exchange engine (REST Ingestion → Kafka / Symbol Routing → Disruptor → MatchingEngine → OrderStatusTracker → Trade Persistence) function cohesively end-to-end.

## Prerequisites

1. Infrastructure services running (`docker-compose up -d` for Kafka, Zookeeper, Redis).
2. Backend application running on `http://localhost:8080`.

## Smoke Test Procedure

### 1. Submit a Maker (Resting) Buy Limit Order
Post a limit buy order that rests in the order book:

```powershell
curl.exe -X POST "http://localhost:8080/orders" `
  -H "Content-Type: application/json" `
  -d '{"orderId":"smoke-buy-1","symbol":"AAPL","side":"BUY","type":"LIMIT","price":150.00,"quantity":100}'
```

Verify the order status is `PENDING`:
```powershell
curl.exe "http://localhost:8080/orders/smoke-buy-1"
```

### 2. Verify Level 2 Order Book Depth
Query the live L2 depth to ensure the bid appears:
```powershell
curl.exe "http://localhost:8080/orders/book/AAPL?depth=5"
```
*Expected*: At least one bid at price `150.00` with quantity `100`.

### 3. Submit a Matching Taker Sell Limit Order
Post an overlapping sell order to trigger an immediate trade:

```powershell
curl.exe -X POST "http://localhost:8080/orders" `
  -H "Content-Type: application/json" `
  -d '{"orderId":"smoke-sell-1","symbol":"AAPL","side":"SELL","type":"LIMIT","price":150.00,"quantity":100}'
```

### 4. Verify Order Status Transitions
Check that both orders transitioned to `FILLED`:
```powershell
curl.exe "http://localhost:8080/orders/smoke-buy-1"
curl.exe "http://localhost:8080/orders/smoke-sell-1"
```
*Expected*: Status = `FILLED` with `remainingQuantity = 0`.

### 5. Verify Trade Persistence in History
Query executed trades for the symbol to ensure asynchronous Kafka-to-database persistence succeeded:
```powershell
curl.exe "http://localhost:8080/history/trades/AAPL"
```
*Expected*: At least one executed trade with `buyerOrderId: smoke-buy-1`, `sellerOrderId: smoke-sell-1`, `price: 150.00`, and `quantity: 100`.
