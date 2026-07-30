package com.apex.exchange.engine.core;

import com.apex.exchange.engine.model.*;
import com.apex.exchange.engine.service.MatchingEngine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OrderBookDepthTest {

    private MatchingEngine matchingEngine;

    @BeforeEach
    void setUp() {
        matchingEngine = new MatchingEngine();
    }

    @Test
    void testOrderBookL2DepthAggregation() {
        // Add buy orders at different and same price levels
        matchingEngine.process(new Order("b1", "AAPL", OrderSide.BUY, OrderType.LIMIT, 150.0, 10, System.nanoTime()));
        matchingEngine.process(new Order("b2", "AAPL", OrderSide.BUY, OrderType.LIMIT, 150.0, 15, System.nanoTime()));
        matchingEngine.process(new Order("b3", "AAPL", OrderSide.BUY, OrderType.LIMIT, 149.0, 30, System.nanoTime()));

        // Add sell orders at different price levels
        matchingEngine.process(new Order("s1", "AAPL", OrderSide.SELL, OrderType.LIMIT, 152.0, 20, System.nanoTime()));
        matchingEngine.process(new Order("s2", "AAPL", OrderSide.SELL, OrderType.LIMIT, 153.0, 40, System.nanoTime()));

        OrderBookDepthDto depth = matchingEngine.getOrderBook().getDepth("AAPL", 5);

        assertEquals("AAPL", depth.getSymbol());
        assertEquals(2, depth.getBids().size());
        assertEquals(2, depth.getAsks().size());

        // Bids sorted descending by price: top bid 150.0 with qty 25 (10 + 15) across 2 orders
        assertEquals(150.0, depth.getBids().get(0).getPrice());
        assertEquals(25, depth.getBids().get(0).getQuantity());
        assertEquals(2, depth.getBids().get(0).getOrderCount());

        assertEquals(149.0, depth.getBids().get(1).getPrice());
        assertEquals(30, depth.getBids().get(1).getQuantity());
        assertEquals(1, depth.getBids().get(1).getOrderCount());

        // Asks sorted ascending by price: top ask 152.0 with qty 20
        assertEquals(152.0, depth.getAsks().get(0).getPrice());
        assertEquals(20, depth.getAsks().get(0).getQuantity());
        assertEquals(1, depth.getAsks().get(0).getOrderCount());

        assertEquals(153.0, depth.getAsks().get(1).getPrice());
        assertEquals(40, depth.getAsks().get(1).getQuantity());
        assertEquals(1, depth.getAsks().get(1).getOrderCount());
    }

    @Test
    void testOrderBookDepthLimitsToMaxDepth() {
        matchingEngine.process(new Order("b1", "AAPL", OrderSide.BUY, OrderType.LIMIT, 150.0, 10, System.nanoTime()));
        matchingEngine.process(new Order("b2", "AAPL", OrderSide.BUY, OrderType.LIMIT, 149.0, 10, System.nanoTime()));
        matchingEngine.process(new Order("b3", "AAPL", OrderSide.BUY, OrderType.LIMIT, 148.0, 10, System.nanoTime()));

        OrderBookDepthDto depth = matchingEngine.getOrderBook().getDepth("AAPL", 2);
        assertEquals(2, depth.getBids().size());
        assertEquals(150.0, depth.getBids().get(0).getPrice());
        assertEquals(149.0, depth.getBids().get(1).getPrice());
    }
}
