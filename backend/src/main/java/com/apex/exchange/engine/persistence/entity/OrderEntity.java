package com.apex.exchange.engine.persistence.entity;

import com.apex.exchange.engine.model.OrderSide;
import com.apex.exchange.engine.model.OrderStatus;
import com.apex.exchange.engine.model.OrderType;
import jakarta.persistence.*;

@Entity
@Table(name = "orders")
public class OrderEntity {

    @Id
    @Column(name = "order_id", nullable = false, unique = true)
    private String orderId;

    @Column(name = "symbol", nullable = false)
    private String symbol;

    @Enumerated(EnumType.STRING)
    @Column(name = "side", nullable = false)
    private OrderSide side;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private OrderType type;

    @Column(name = "price")
    private double price;

    @Column(name = "initial_quantity", nullable = false)
    private long initialQuantity;

    @Column(name = "executed_quantity", nullable = false)
    private long executedQuantity;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private OrderStatus status;

    @Column(name = "timestamp", nullable = false)
    private long timestamp;

    public OrderEntity() {}

    public OrderEntity(String orderId, String symbol, OrderSide side, OrderType type,
                       double price, long initialQuantity, long executedQuantity,
                       OrderStatus status, long timestamp) {
        this.orderId = orderId;
        this.symbol = symbol;
        this.side = side;
        this.type = type;
        this.price = price;
        this.initialQuantity = initialQuantity;
        this.executedQuantity = executedQuantity;
        this.status = status;
        this.timestamp = timestamp;
    }

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }

    public OrderSide getSide() { return side; }
    public void setSide(OrderSide side) { this.side = side; }

    public OrderType getType() { return type; }
    public void setType(OrderType type) { this.type = type; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public long getInitialQuantity() { return initialQuantity; }
    public void setInitialQuantity(long initialQuantity) { this.initialQuantity = initialQuantity; }

    public long getExecutedQuantity() { return executedQuantity; }
    public void setExecutedQuantity(long executedQuantity) { this.executedQuantity = executedQuantity; }

    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus status) { this.status = status; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}
