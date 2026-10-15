package com.apex.exchange.engine.core;

import com.apex.exchange.engine.model.Order;
import com.apex.exchange.engine.model.OrderBookDepthDto;
import com.apex.exchange.engine.model.OrderSide;
import com.apex.exchange.engine.model.PriceLevelDto;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * High-performance OrderBook with O(1) order additions and cancellations.
 * Backed by sorted PriceLevels containing intrusive doubly-linked lists of Orders.
 */
public class OrderBook {

    private final TreeMap<Double, PriceLevel> buyLevels;
    private final TreeMap<Double, PriceLevel> sellLevels;
    private final Map<String, Order> activeOrders = new HashMap<>();

    private final OrderBookSide buySide;
    private final OrderBookSide sellSide;

    public OrderBook() {
        this.buyLevels = new TreeMap<>(Comparator.reverseOrder()); // Highest bids first
        this.sellLevels = new TreeMap<>(Comparator.naturalOrder()); // Lowest asks first
        this.buySide = new OrderBookSide(buyLevels, OrderSide.BUY);
        this.sellSide = new OrderBookSide(sellLevels, OrderSide.SELL);
    }

    public void addBuyOrder(Order order) {
        activeOrders.put(order.getOrderId(), order);
        PriceLevel level = buyLevels.computeIfAbsent(order.getPrice(), PriceLevel::new);
        level.addOrder(order);
    }

    public void addSellOrder(Order order) {
        activeOrders.put(order.getOrderId(), order);
        PriceLevel level = sellLevels.computeIfAbsent(order.getPrice(), PriceLevel::new);
        level.addOrder(order);
    }

    public void removeActiveOrder(String orderId) {
        Order order = activeOrders.remove(orderId);
        if (order != null) {
            order.removeFromLevel();
            cleanEmptyLevel(order.getSide(), order.getPrice());
        }
    }

    /**
     * Cancels an order in O(1) time without performing linear scans.
     */
    public boolean cancelOrder(String orderId) {
        Order order = activeOrders.remove(orderId);
        if (order == null) {
            return false;
        }

        order.cancel(); // order.cancel() unlinks from its PriceLevel in O(1)
        cleanEmptyLevel(order.getSide(), order.getPrice());
        return true;
    }

    private void cleanEmptyLevel(OrderSide side, double price) {
        TreeMap<Double, PriceLevel> levels = (side == OrderSide.BUY) ? buyLevels : sellLevels;
        PriceLevel level = levels.get(price);
        if (level != null && level.isEmpty()) {
            levels.remove(price);
        }
    }

    /**
     * Aggregates top-N price level depth in O(depth) time using precomputed level quantities.
     * Eliminates full-book scans and temporary collection allocations.
     */
    public OrderBookDepthDto getDepth(String symbol, int depth) {
        List<PriceLevelDto> bids = collectDepth(buyLevels, depth);
        List<PriceLevelDto> asks = collectDepth(sellLevels, depth);
        return new OrderBookDepthDto(symbol, System.currentTimeMillis(), bids, asks);
    }

    private List<PriceLevelDto> collectDepth(TreeMap<Double, PriceLevel> levels, int depth) {
        List<PriceLevelDto> result = new ArrayList<>(Math.min(depth, levels.size()));
        int count = 0;
        for (PriceLevel level : levels.values()) {
            if (level.isEmpty() || level.getTotalQuantity() <= 0) {
                continue;
            }
            result.add(new PriceLevelDto(level.getPrice(), level.getTotalQuantity(), level.getOrderCount()));
            count++;
            if (count >= depth) {
                break;
            }
        }
        return result;
    }

    public Order bestBid() {
        return buySide.peek();
    }

    public Order bestAsk() {
        return sellSide.peek();
    }

    public OrderBookSide getBuyOrders() {
        return buySide;
    }

    public OrderBookSide getSellOrders() {
        return sellSide;
    }

    public Order getActiveOrder(String orderId) {
        return activeOrders.get(orderId);
    }

    public int getActiveOrderCount() {
        return activeOrders.size();
    }
}