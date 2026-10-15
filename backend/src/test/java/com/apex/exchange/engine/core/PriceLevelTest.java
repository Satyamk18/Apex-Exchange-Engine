package com.apex.exchange.engine.core;

import com.apex.exchange.engine.model.Order;
import com.apex.exchange.engine.model.OrderSide;
import com.apex.exchange.engine.model.OrderType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PriceLevelTest {

    private PriceLevel level;

    @BeforeEach
    void setUp() {
        level = new PriceLevel(150.0);
    }

    @Test
    void testEmptyLevelInitialization() {
        assertTrue(level.isEmpty());
        assertEquals(0, level.getOrderCount());
        assertEquals(0, level.getTotalQuantity());
        assertEquals(150.0, level.getPrice());
        assertNull(level.peekHead());
    }

    @Test
    void testAddOrdersFIFO() {
        Order o1 = new Order("o1", "AAPL", OrderSide.BUY, OrderType.LIMIT, 150.0, 10, 1000L);
        Order o2 = new Order("o2", "AAPL", OrderSide.BUY, OrderType.LIMIT, 150.0, 20, 2000L);

        level.addOrder(o1);
        level.addOrder(o2);

        assertFalse(level.isEmpty());
        assertEquals(2, level.getOrderCount());
        assertEquals(30, level.getTotalQuantity());
        assertSame(o1, level.peekHead());
        assertSame(o2, level.getTail());
        assertSame(o2, o1.getNext());
        assertSame(o1, o2.getPrev());
        assertSame(level, o1.getParentLevel());
        assertSame(level, o2.getParentLevel());
    }

    @Test
    void testRemoveMiddleOrder() {
        Order o1 = new Order("o1", "AAPL", OrderSide.BUY, OrderType.LIMIT, 150.0, 10, 1000L);
        Order o2 = new Order("o2", "AAPL", OrderSide.BUY, OrderType.LIMIT, 150.0, 20, 2000L);
        Order o3 = new Order("o3", "AAPL", OrderSide.BUY, OrderType.LIMIT, 150.0, 30, 3000L);

        level.addOrder(o1);
        level.addOrder(o2);
        level.addOrder(o3);

        boolean removed = level.removeOrder(o2);
        assertTrue(removed);
        assertEquals(2, level.getOrderCount());
        assertEquals(40, level.getTotalQuantity());

        assertSame(o3, o1.getNext());
        assertSame(o1, o3.getPrev());
        assertNull(o2.getParentLevel());
        assertNull(o2.getPrev());
        assertNull(o2.getNext());
    }

    @Test
    void testRemoveHeadOrder() {
        Order o1 = new Order("o1", "AAPL", OrderSide.BUY, OrderType.LIMIT, 150.0, 10, 1000L);
        Order o2 = new Order("o2", "AAPL", OrderSide.BUY, OrderType.LIMIT, 150.0, 20, 2000L);

        level.addOrder(o1);
        level.addOrder(o2);

        boolean removed = level.removeOrder(o1);
        assertTrue(removed);
        assertEquals(1, level.getOrderCount());
        assertEquals(20, level.getTotalQuantity());
        assertSame(o2, level.peekHead());
        assertSame(o2, level.getTail());
        assertNull(o2.getPrev());
    }

    @Test
    void testRemoveTailOrder() {
        Order o1 = new Order("o1", "AAPL", OrderSide.BUY, OrderType.LIMIT, 150.0, 10, 1000L);
        Order o2 = new Order("o2", "AAPL", OrderSide.BUY, OrderType.LIMIT, 150.0, 20, 2000L);

        level.addOrder(o1);
        level.addOrder(o2);

        boolean removed = level.removeOrder(o2);
        assertTrue(removed);
        assertEquals(1, level.getOrderCount());
        assertEquals(10, level.getTotalQuantity());
        assertSame(o1, level.peekHead());
        assertSame(o1, level.getTail());
        assertNull(o1.getNext());
    }

    @Test
    void testPollHeadEmptiesLevel() {
        Order o1 = new Order("o1", "AAPL", OrderSide.BUY, OrderType.LIMIT, 150.0, 10, 1000L);
        level.addOrder(o1);

        Order polled = level.pollHead();
        assertSame(o1, polled);
        assertTrue(level.isEmpty());
        assertEquals(0, level.getOrderCount());
        assertEquals(0, level.getTotalQuantity());
        assertNull(level.peekHead());
        assertNull(level.getTail());
    }

    @Test
    void testDeductQuantity() {
        Order o1 = new Order("o1", "AAPL", OrderSide.BUY, OrderType.LIMIT, 150.0, 100, 1000L);
        level.addOrder(o1);

        level.deductQuantity(30);
        assertEquals(70, level.getTotalQuantity());
        assertEquals(1, level.getOrderCount());
    }
}
