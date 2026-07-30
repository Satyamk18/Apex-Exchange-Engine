package com.apex.exchange.engine.service;

import com.apex.exchange.engine.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class OrderCancellationTest {

    private MatchingEngine matchingEngine;
    private OrderStatusTracker tracker;

    @BeforeEach
    void setUp() {
        matchingEngine = new MatchingEngine();
        tracker = new OrderStatusTracker();
    }

    @Test
    void testCancelRestingLimitBuyOrder() {
        Order buyOrder = new Order("b1", "AAPL", OrderSide.BUY, OrderType.LIMIT, 150.0, 10, System.nanoTime());
        matchingEngine.process(buyOrder, tracker);

        Optional<OrderStatusResponse> statusBefore = tracker.getOrderStatus("b1");
        assertTrue(statusBefore.isPresent());
        assertEquals(OrderStatus.NEW, statusBefore.get().getStatus());
        assertEquals(1, matchingEngine.getOrderBook().getBuyOrders().size());

        boolean cancelled = matchingEngine.cancelOrder("b1", tracker);
        assertTrue(cancelled);

        Optional<OrderStatusResponse> statusAfter = tracker.getOrderStatus("b1");
        assertTrue(statusAfter.isPresent());
        assertEquals(OrderStatus.CANCELLED, statusAfter.get().getStatus());
        assertEquals(0, statusAfter.get().getRemainingQuantity());
        assertTrue(matchingEngine.getOrderBook().getBuyOrders().isEmpty());
    }

    @Test
    void testCancelNonExistentOrderReturnsFalse() {
        boolean cancelled = matchingEngine.cancelOrder("non-existent", tracker);
        assertFalse(cancelled);
    }

    @Test
    void testPartialFillThenCancel() {
        Order sellLimit = new Order("s1", "AAPL", OrderSide.SELL, OrderType.LIMIT, 100.0, 20, System.nanoTime());
        matchingEngine.process(sellLimit, tracker);

        Order buyLimit = new Order("b1", "AAPL", OrderSide.BUY, OrderType.LIMIT, 100.0, 5, System.nanoTime());
        matchingEngine.process(buyLimit, tracker);

        Optional<OrderStatusResponse> s1Status = tracker.getOrderStatus("s1");
        assertTrue(s1Status.isPresent());
        assertEquals(OrderStatus.PARTIALLY_FILLED, s1Status.get().getStatus());
        assertEquals(5, s1Status.get().getExecutedQuantity());
        assertEquals(15, s1Status.get().getRemainingQuantity());

        boolean cancelled = matchingEngine.cancelOrder("s1", tracker);
        assertTrue(cancelled);

        Optional<OrderStatusResponse> s1StatusFinal = tracker.getOrderStatus("s1");
        assertTrue(s1StatusFinal.isPresent());
        assertEquals(OrderStatus.CANCELLED, s1StatusFinal.get().getStatus());
        assertEquals(0, s1StatusFinal.get().getRemainingQuantity());
        assertTrue(matchingEngine.getOrderBook().getSellOrders().isEmpty());
    }
}
