package com.apex.exchange.engine.core;

import com.apex.exchange.engine.model.*;

import java.util.*;
import java.util.stream.Collectors;

public class OrderBook {

    private final PriorityQueue<Order> buyBook;
    private final PriorityQueue<Order> sellBook;
    private final Map<String, Order> activeOrders = new HashMap<>();

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

    public void addBuyOrder(Order order) {
        buyBook.offer(order);
        activeOrders.put(order.getOrderId(), order);
    }

    public void addSellOrder(Order order) {
        sellBook.offer(order);
        activeOrders.put(order.getOrderId(), order);
    }

    public void removeActiveOrder(String orderId) {
        activeOrders.remove(orderId);
    }

    public boolean cancelOrder(String orderId) {
        Order order = activeOrders.remove(orderId);
        if (order == null) {
            return false;
        }

        order.cancel();
        if (order.getSide() == OrderSide.BUY) {
            buyBook.remove(order);
        } else {
            sellBook.remove(order);
        }
        return true;
    }

    public OrderBookDepthDto getDepth(String symbol, int depth) {
        List<PriceLevelDto> bids = aggregateLevels(buyBook, depth, true);
        List<PriceLevelDto> asks = aggregateLevels(sellBook, depth, false);

        return new OrderBookDepthDto(symbol, System.currentTimeMillis(), bids, asks);
    }

    private List<PriceLevelDto> aggregateLevels(PriorityQueue<Order> book, int depth, boolean isBuy) {
        Comparator<Double> comparator = isBuy ? Comparator.reverseOrder() : Comparator.naturalOrder();
        Map<Double, PriceLevelDto> map = new TreeMap<>(comparator);

        for (Order order : book) {
            if (order.getQuantity() <= 0 || order.getStatus() == OrderStatus.CANCELLED) {
                continue;
            }
            PriceLevelDto level = map.computeIfAbsent(
                    order.getPrice(),
                    p -> new PriceLevelDto(p, 0, 0)
            );
            level.setQuantity(level.getQuantity() + order.getQuantity());
            level.setOrderCount(level.getOrderCount() + 1);
        }

        return map.values().stream()
                .limit(depth)
                .collect(Collectors.toList());
    }

    public PriorityQueue<Order> getBuyOrders() {
        return buyBook;
    }

    public PriorityQueue<Order> getSellOrders() {
        return sellBook;
    }

    public Map<String, Order> getActiveOrders() {
        return activeOrders;
    }
}