package com.apex.exchange.engine.core;

import com.apex.exchange.engine.model.Order;

import java.util.PriorityQueue;

public class OrderBook {

    private final PriorityQueue<Order> buyBook;
    private final PriorityQueue<Order> sellBook;

    public OrderBook() {
        buyBook = new PriorityQueue<>(
                (o1, o2) -> {
                    int priceCompare = Double.compare(o2.getPrice(), o1.getPrice());
                    if (priceCompare == 0) {
                        return Long.compare(o1.getTimestamp(), o2.getTimestamp());
                    }
                    return priceCompare;
                }
        );

        sellBook = new PriorityQueue<>(
                (o1, o2) -> {
                    int priceCompare = Double.compare(o1.getPrice(), o2.getPrice());
                    if (priceCompare == 0) {
                        return Long.compare(o1.getTimestamp(), o2.getTimestamp());
                    }
                    return priceCompare;
                }
        );
    }

    public PriorityQueue<Order> getBuyBook() {
        return buyBook;
    }

    public PriorityQueue<Order> getSellBook() {
        return sellBook;
    }
}