package com.apex.exchange.engine.core;

import com.apex.exchange.engine.model.Order;
import com.apex.exchange.engine.model.OrderSide;

import java.util.AbstractCollection;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.TreeMap;

/**
 * High-performance Collection view over a single side (Bids or Asks) of the OrderBook.
 * Backed by PriceLevel intrusive lists in a TreeMap, providing O(1) peek/poll operations
 * and strict price-time iteration without copying or sorting.
 */
public class OrderBookSide extends AbstractCollection<Order> {

    private final TreeMap<Double, PriceLevel> levels;
    private final OrderSide side;

    public OrderBookSide(TreeMap<Double, PriceLevel> levels, OrderSide side) {
        this.levels = levels;
        this.side = side;
    }

    /**
     * Returns the highest-priority order on this side in O(1) without removing it.
     */
    public Order peek() {
        while (!levels.isEmpty()) {
            PriceLevel level = levels.firstEntry().getValue();
            Order head = level.peekHead();
            if (head != null) {
                return head;
            }
            levels.pollFirstEntry(); // Clean up empty level
        }
        return null;
    }

    /**
     * Unlinks and returns the highest-priority order on this side in O(1).
     */
    public Order poll() {
        while (!levels.isEmpty()) {
            Map.Entry<Double, PriceLevel> entry = levels.firstEntry();
            PriceLevel level = entry.getValue();
            Order head = level.pollHead();
            if (head != null) {
                if (level.isEmpty()) {
                    levels.remove(entry.getKey());
                }
                return head;
            }
            levels.pollFirstEntry(); // Clean up empty level
        }
        return null;
    }

    @Override
    public boolean isEmpty() {
        return peek() == null;
    }

    @Override
    public int size() {
        int total = 0;
        for (PriceLevel level : levels.values()) {
            total += level.getOrderCount();
        }
        return total;
    }

    @Override
    public boolean contains(Object o) {
        if (!(o instanceof Order)) return false;
        Order order = (Order) o;
        PriceLevel level = levels.get(order.getPrice());
        if (level == null) return false;
        return order.getParentLevel() == level;
    }

    @Override
    public boolean remove(Object o) {
        if (!(o instanceof Order)) return false;
        Order order = (Order) o;
        PriceLevel level = levels.get(order.getPrice());
        if (level != null && level.removeOrder(order)) {
            if (level.isEmpty()) {
                levels.remove(order.getPrice());
            }
            return true;
        }
        return false;
    }

    @Override
    public void clear() {
        levels.clear();
    }

    @Override
    public Iterator<Order> iterator() {
        return new OrderBookSideIterator(levels);
    }

    public OrderSide getSide() {
        return side;
    }

    private static class OrderBookSideIterator implements Iterator<Order> {
        private final Iterator<PriceLevel> levelIterator;
        private Order currentOrder;
        private Order nextOrder;

        public OrderBookSideIterator(TreeMap<Double, PriceLevel> levels) {
            this.levelIterator = levels.values().iterator();
            advance();
        }

        private void advance() {
            if (currentOrder != null && currentOrder.getNext() != null) {
                nextOrder = currentOrder.getNext();
                return;
            }
            while (levelIterator.hasNext()) {
                PriceLevel nextLevel = levelIterator.next();
                if (!nextLevel.isEmpty() && nextLevel.getHead() != null) {
                    nextOrder = nextLevel.getHead();
                    return;
                }
            }
            nextOrder = null;
        }

        @Override
        public boolean hasNext() {
            return nextOrder != null;
        }

        @Override
        public Order next() {
            if (nextOrder == null) {
                throw new NoSuchElementException();
            }
            currentOrder = nextOrder;
            advance();
            return currentOrder;
        }
    }
}
