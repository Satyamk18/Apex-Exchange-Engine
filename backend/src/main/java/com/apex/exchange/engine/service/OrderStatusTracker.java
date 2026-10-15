package com.apex.exchange.engine.service;

import com.apex.exchange.engine.model.Order;
import com.apex.exchange.engine.model.OrderStatus;
import com.apex.exchange.engine.model.OrderStatusResponse;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Thread-safe order lifecycle tracker with bounded memory retention for terminal orders.
 * Prevents memory exhaustion under sustained high-throughput workloads.
 */
@Service
public class OrderStatusTracker {

    private static final int MAX_TERMINAL_ORDERS = 50_000;

    private final Map<String, OrderStatusResponse> orderStatuses = new ConcurrentHashMap<>();
    private final ConcurrentLinkedQueue<String> terminalOrderQueue = new ConcurrentLinkedQueue<>();
    private final AtomicInteger terminalOrderCount = new AtomicInteger(0);

    public void registerOrder(Order order) {
        OrderStatusResponse response = new OrderStatusResponse(
                order.getOrderId(),
                order.getSymbol(),
                order.getSide(),
                order.getType(),
                order.getPrice(),
                order.getInitialQuantity(),
                order.getExecutedQuantity(),
                order.getQuantity(),
                order.getStatus(),
                order.getTimestamp()
        );
        orderStatuses.put(order.getOrderId(), response);
        if (order.getStatus() == OrderStatus.FILLED || order.getStatus() == OrderStatus.CANCELLED) {
            trackTerminalOrder(order.getOrderId());
        }
    }

    public void updateOrder(Order order) {
        OrderStatusResponse response = orderStatuses.get(order.getOrderId());
        if (response != null) {
            response.setExecutedQuantity(order.getExecutedQuantity());
            response.setRemainingQuantity(order.getQuantity());
            response.setStatus(order.getStatus());
        } else {
            registerOrder(order);
        }

        if (order.getStatus() == OrderStatus.FILLED || order.getStatus() == OrderStatus.CANCELLED) {
            trackTerminalOrder(order.getOrderId());
        }
    }

    public void markCancelled(String orderId) {
        OrderStatusResponse response = orderStatuses.get(orderId);
        if (response != null) {
            response.setRemainingQuantity(0);
            response.setStatus(OrderStatus.CANCELLED);
            trackTerminalOrder(orderId);
        }
    }

    public Optional<OrderStatusResponse> getOrderStatus(String orderId) {
        return Optional.ofNullable(orderStatuses.get(orderId));
    }

    private void trackTerminalOrder(String orderId) {
        terminalOrderQueue.offer(orderId);
        if (terminalOrderCount.incrementAndGet() > MAX_TERMINAL_ORDERS) {
            String oldestId = terminalOrderQueue.poll();
            if (oldestId != null) {
                OrderStatusResponse old = orderStatuses.get(oldestId);
                if (old != null && (old.getStatus() == OrderStatus.FILLED || old.getStatus() == OrderStatus.CANCELLED)) {
                    orderStatuses.remove(oldestId);
                }
                terminalOrderCount.decrementAndGet();
            }
        }
    }

    public int size() {
        return orderStatuses.size();
    }
}
