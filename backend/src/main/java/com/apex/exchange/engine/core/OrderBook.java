package com.apex.exchange.engine.core;

import com.apex.exchange.engine.model.Order;
import com.apex.exchange.engine.model.OrderSide;

import java.util.Comparator;
import java.util.PriorityQueue;

public class OrderBook {

    private final PriorityQueue<Order> buyOrders;
    private final PriorityQueue<Order> sellOrders;

    public OrderBook() {

        // Highest price first, then earliest timestamp
        buyOrders = new PriorityQueue<>(
                Comparator
                        .comparingDouble(Order::getPrice).reversed()
                        .thenComparingLong(Order::getTimestamp)
        );

        // Lowest price first, then earliest timestamp
        sellOrders = new PriorityQueue<>(
                Comparator
                        .comparingDouble(Order::getPrice)
                        .thenComparingLong(Order::getTimestamp)
        );
    }

    public void addOrder(Order order) {
        if (order.getSide() == OrderSide.BUY) {
            buyOrders.add(order);
        } else {
            sellOrders.add(order);
        }
    }

    public PriorityQueue<Order> getBuyOrders() {
        return buyOrders;
    }

    public PriorityQueue<Order> getSellOrders() {
        return sellOrders;
    }
}