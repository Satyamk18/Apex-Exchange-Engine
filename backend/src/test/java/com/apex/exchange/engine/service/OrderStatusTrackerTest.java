package com.apex.exchange.engine.service;

import com.apex.exchange.engine.model.Order;
import com.apex.exchange.engine.model.OrderSide;
import com.apex.exchange.engine.model.OrderStatus;
import com.apex.exchange.engine.model.OrderType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OrderStatusTrackerTest {

    private OrderStatusTracker tracker;

    @BeforeEach
    void setUp() {
        tracker = new OrderStatusTracker();
    }

    @Test
    void testActiveOrderLifecycle() {
        Order order = new Order("ord-1", "AAPL", OrderSide.BUY, OrderType.LIMIT, 150.0, 100, 1000L);
        tracker.registerOrder(order);

        assertTrue(tracker.getOrderStatus("ord-1").isPresent());
        assertEquals(OrderStatus.NEW, tracker.getOrderStatus("ord-1").get().getStatus());
        assertEquals(100, tracker.getOrderStatus("ord-1").get().getRemainingQuantity());

        order.reduceQuantity(40);
        tracker.updateOrder(order);
        assertEquals(OrderStatus.PARTIALLY_FILLED, tracker.getOrderStatus("ord-1").get().getStatus());
        assertEquals(40, tracker.getOrderStatus("ord-1").get().getExecutedQuantity());
        assertEquals(60, tracker.getOrderStatus("ord-1").get().getRemainingQuantity());

        tracker.markCancelled("ord-1");
        assertEquals(OrderStatus.CANCELLED, tracker.getOrderStatus("ord-1").get().getStatus());
        assertEquals(0, tracker.getOrderStatus("ord-1").get().getRemainingQuantity());
    }

    @Test
    void testDeterministicTradeIdFormatting() {
        MatchingEngine engine = new MatchingEngine();
        Order sell = new Order("s1", "AAPL", OrderSide.SELL, OrderType.LIMIT, 100.0, 10, 1000L);
        engine.process(sell);

        Order buy = new Order("b1", "AAPL", OrderSide.BUY, OrderType.LIMIT, 100.0, 10, 2000L);
        var trades = engine.process(buy);

        assertEquals(1, trades.size());
        assertEquals("AAPL-T1", trades.get(0).getTradeId());
    }
}
