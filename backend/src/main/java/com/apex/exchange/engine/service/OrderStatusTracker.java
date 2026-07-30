package com.apex.exchange.engine.service;

import com.apex.exchange.engine.model.Order;
import com.apex.exchange.engine.model.OrderStatus;
import com.apex.exchange.engine.model.OrderStatusResponse;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class OrderStatusTracker {

    private final Map<String, OrderStatusResponse> orderStatuses = new ConcurrentHashMap<>();

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
    }

    public void markCancelled(String orderId) {
        OrderStatusResponse response = orderStatuses.get(orderId);
        if (response != null) {
            response.setRemainingQuantity(0);
            response.setStatus(OrderStatus.CANCELLED);
        }
    }

    public void markRejected(Order order) {
        order.setStatus(OrderStatus.REJECTED);
        registerOrder(order);
    }

    public Optional<OrderStatusResponse> getOrderStatus(String orderId) {
        return Optional.ofNullable(orderStatuses.get(orderId));
    }
}
