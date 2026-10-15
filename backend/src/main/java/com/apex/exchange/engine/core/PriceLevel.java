package com.apex.exchange.engine.core;

import com.apex.exchange.engine.model.Order;

/**
 * Manages an intrusive FIFO doubly-linked list of resting Order nodes
 * for a specific price level. Provides O(1) order insertion, O(1) removal,
 * and O(1) aggregate quantity and order count maintenance.
 */
public class PriceLevel {

    private final double price;
    private long totalQuantity;
    private int orderCount;
    private Order head;
    private Order tail;

    public PriceLevel(double price) {
        this.price = price;
        this.totalQuantity = 0;
        this.orderCount = 0;
        this.head = null;
        this.tail = null;
    }

    /**
     * Appends an order to the tail of this price level (FIFO priority) in O(1).
     */
    public void addOrder(Order order) {
        if (order == null) return;
        order.setParentLevel(this);
        order.setPrev(tail);
        order.setNext(null);

        if (tail != null) {
            tail.setNext(order);
        } else {
            head = order;
        }
        tail = order;

        totalQuantity += order.getQuantity();
        orderCount++;
    }

    /**
     * Unlinks an order from this price level in O(1).
     * Correctly handles head, tail, or middle removals without linear scanning.
     */
    public boolean removeOrder(Order order) {
        if (order == null || order.getParentLevel() != this) {
            return false;
        }

        Order prev = order.getPrev();
        Order next = order.getNext();

        if (prev != null) {
            prev.setNext(next);
        } else {
            head = next;
        }

        if (next != null) {
            next.setPrev(prev);
        } else {
            tail = prev;
        }

        order.setPrev(null);
        order.setNext(null);
        order.setParentLevel(null);

        totalQuantity -= order.getQuantity();
        if (totalQuantity < 0) totalQuantity = 0;
        orderCount--;
        return true;
    }

    /**
     * Deducts traded quantity from this price level when an order is partially executed.
     */
    public void deductQuantity(long qty) {
        this.totalQuantity -= qty;
        if (this.totalQuantity < 0) {
            this.totalQuantity = 0;
        }
    }

    /**
     * Returns the highest-priority resting order at this price level in O(1).
     */
    public Order peekHead() {
        return head;
    }

    /**
     * Unlinks and returns the highest-priority resting order at this price level in O(1).
     */
    public Order pollHead() {
        if (head == null) {
            return null;
        }
        Order order = head;
        removeOrder(order);
        return order;
    }

    public boolean isEmpty() {
        return orderCount == 0 || head == null;
    }

    public double getPrice() {
        return price;
    }

    public long getTotalQuantity() {
        return totalQuantity;
    }

    public int getOrderCount() {
        return orderCount;
    }

    public Order getHead() {
        return head;
    }

    public Order getTail() {
        return tail;
    }
}
