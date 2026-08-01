package com.apex.exchange.engine.snapshot;

import com.apex.exchange.engine.model.*;
import com.apex.exchange.engine.service.MatchingEngine;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SnapshotRecoveryTest {

    @TempDir
    Path tempDir;

    private SnapshotService snapshotService;
    private MatchingEngine engine;

    @BeforeEach
    void setUp() {
        snapshotService = new SnapshotService(tempDir.toString());
        engine = new MatchingEngine();
    }

    @Test
    void testSnapshotSaveAndLoad() {
        // Add some resting limit orders
        engine.process(new Order("b1", "AAPL", OrderSide.BUY, OrderType.LIMIT, 150.0, 10, System.nanoTime()));
        engine.process(new Order("b2", "AAPL", OrderSide.BUY, OrderType.LIMIT, 149.0, 20, System.nanoTime()));
        engine.process(new Order("s1", "AAPL", OrderSide.SELL, OrderType.LIMIT, 155.0, 15, System.nanoTime()));

        snapshotService.saveSnapshot("AAPL", engine, 100L);

        assertTrue(snapshotService.hasSnapshot("AAPL"));

        OrderBookSnapshot snapshot = snapshotService.loadSnapshot("AAPL");
        assertNotNull(snapshot);
        assertEquals("AAPL", snapshot.getSymbol());
        assertEquals(100L, snapshot.getSnapshotSequence());
        assertEquals(2, snapshot.getBuyOrders().size());
        assertEquals(1, snapshot.getSellOrders().size());
    }

    @Test
    void testRecoveryRestoresOrderBookState() {
        // Populate and snapshot engine
        engine.process(new Order("b1", "AAPL", OrderSide.BUY, OrderType.LIMIT, 150.0, 10, System.nanoTime()));
        engine.process(new Order("s1", "AAPL", OrderSide.SELL, OrderType.LIMIT, 155.0, 5, System.nanoTime()));

        snapshotService.saveSnapshot("AAPL", engine, 50L);

        // Create fresh engine and restore from snapshot
        MatchingEngine recoveredEngine = new MatchingEngine();
        OrderBookSnapshot snapshot = snapshotService.loadSnapshot("AAPL");

        assertNotNull(snapshot);
        for (Order order : snapshot.getBuyOrders()) {
            recoveredEngine.getOrderBook().addBuyOrder(order);
        }
        for (Order order : snapshot.getSellOrders()) {
            recoveredEngine.getOrderBook().addSellOrder(order);
        }

        // Verify state matches original
        assertEquals(1, recoveredEngine.getOrderBook().getBuyOrders().size());
        assertEquals(1, recoveredEngine.getOrderBook().getSellOrders().size());
        assertEquals(150.0, recoveredEngine.getOrderBook().getBuyOrders().peek().getPrice());
        assertEquals(155.0, recoveredEngine.getOrderBook().getSellOrders().peek().getPrice());

        // Verify recovered engine can still match new orders
        List<Trade> trades = recoveredEngine.process(
                new Order("b2", "AAPL", OrderSide.BUY, OrderType.LIMIT, 155.0, 5, System.nanoTime())
        );
        assertEquals(1, trades.size());
        assertEquals(155.0, trades.get(0).getPrice());
        assertEquals(5, trades.get(0).getQuantity());
    }

    @Test
    void testAvailableSymbols() {
        engine.process(new Order("b1", "AAPL", OrderSide.BUY, OrderType.LIMIT, 150.0, 10, System.nanoTime()));
        snapshotService.saveSnapshot("AAPL", engine, 1L);

        MatchingEngine engine2 = new MatchingEngine();
        engine2.process(new Order("b2", "TSLA", OrderSide.BUY, OrderType.LIMIT, 200.0, 5, System.nanoTime()));
        snapshotService.saveSnapshot("TSLA", engine2, 2L);

        List<String> symbols = snapshotService.availableSymbols();
        assertEquals(2, symbols.size());
        assertTrue(symbols.contains("AAPL"));
        assertTrue(symbols.contains("TSLA"));
    }
}
