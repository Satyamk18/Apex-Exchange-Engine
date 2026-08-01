package com.apex.exchange.engine.snapshot;

import com.apex.exchange.engine.model.Order;
import com.apex.exchange.engine.service.MatchingEngine;
import com.apex.exchange.engine.service.MatchingEngineManager;
import com.apex.exchange.engine.service.OrderStatusTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EngineRecoveryService {

    private static final Logger log = LoggerFactory.getLogger(EngineRecoveryService.class);

    private final SnapshotService snapshotService;
    private final MatchingEngineManager matchingEngineManager;
    private final OrderStatusTracker orderStatusTracker;

    public EngineRecoveryService(SnapshotService snapshotService,
                                 MatchingEngineManager matchingEngineManager,
                                 OrderStatusTracker orderStatusTracker) {
        this.snapshotService = snapshotService;
        this.matchingEngineManager = matchingEngineManager;
        this.orderStatusTracker = orderStatusTracker;
    }

    /**
     * Runs after the application context is fully loaded and ready.
     * Scans for existing snapshots and restores order book state for each symbol.
     */
    @EventListener(ApplicationReadyEvent.class)
    public void recoverFromSnapshots() {
        log.info("Starting engine recovery from snapshots...");

        List<String> symbols = snapshotService.availableSymbols();

        if (symbols.isEmpty()) {
            log.info("No snapshots found. Starting with empty order books.");
            return;
        }

        for (String symbol : symbols) {
            recoverSymbol(symbol);
        }

        log.info("Engine recovery complete. Restored {} symbol(s).", symbols.size());
    }

    private void recoverSymbol(String symbol) {
        OrderBookSnapshot snapshot = snapshotService.loadSnapshot(symbol);
        if (snapshot == null) {
            return;
        }

        log.info("Recovering symbol={} from snapshot sequence={}", symbol, snapshot.getSnapshotSequence());

        MatchingEngine engine = matchingEngineManager.getOrCreateEngine(symbol);

        restoreOrders(snapshot.getBuyOrders(), engine, true);
        restoreOrders(snapshot.getSellOrders(), engine, false);

        log.info("Recovered symbol={}: buyOrders={}, sellOrders={}",
                symbol,
                snapshot.getBuyOrders() != null ? snapshot.getBuyOrders().size() : 0,
                snapshot.getSellOrders() != null ? snapshot.getSellOrders().size() : 0);
    }

    private void restoreOrders(List<Order> orders, MatchingEngine engine, boolean isBuy) {
        if (orders == null) return;
        for (Order order : orders) {
            if (isBuy) {
                engine.getOrderBook().addBuyOrder(order);
            } else {
                engine.getOrderBook().addSellOrder(order);
            }
            // Re-register in-memory status so the status endpoint responds correctly
            if (orderStatusTracker.getOrderStatus(order.getOrderId()).isEmpty()) {
                orderStatusTracker.registerOrder(order);
            }
        }
    }
}
