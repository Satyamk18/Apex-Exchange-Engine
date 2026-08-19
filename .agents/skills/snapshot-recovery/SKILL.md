---
name: snapshot-recovery
description: Manage, validate, and verify order book state snapshot creation and startup recovery.
---

# Snapshot Recovery Skill Runbook

This skill outlines how state persistence, automated order book snapshot generation, and recovery testing are managed in Apex Exchange Engine.

## Snapshot Location & Mechanism

- **Directory**: `.snapshots/` (located at workspace root or active working directory).
- **Format**: JSON serialized state files containing current active bids and asks for symbol partitions.

## Procedures

### 1. Snapshot Verification
Check if snapshots exist and are structurally valid:

```powershell
ls .snapshots/
```

### 2. Startup State Recovery Testing
1. Submit test limit orders to populate the order book.
2. Trigger snapshot generation or graceful system shutdown.
3. Restart backend service:
   ```powershell
   cd backend
   ./mvnw spring-boot:run
   ```
4. Query depth endpoints (e.g. `/api/v1/orders/depth/{symbol}`) to verify restored bids and asks.
