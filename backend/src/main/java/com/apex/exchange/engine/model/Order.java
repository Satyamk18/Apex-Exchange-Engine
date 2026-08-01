package com.apex.exchange.engine.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Order {

    private final String orderId;
    private final String symbol;
    private final OrderSide side;
    private final OrderType type;
    private final double price;
    private final long initialQuantity;
    private long quantity; // remaining quantity
    private long executedQuantity;
    private OrderStatus status;
    private final long timestamp;

    @JsonCreator
    public Order(@JsonProperty("orderId") String orderId,
                 @JsonProperty("symbol") String symbol,
                 @JsonProperty("side") OrderSide side,
                 @JsonProperty("type") OrderType type,
                 @JsonProperty("price") double price,
                 @JsonProperty("quantity") long quantity,
                 @JsonProperty("timestamp") long timestamp) {
        this.orderId = orderId;
        this.symbol = symbol;
        this.side = side;
        this.type = type != null ? type : OrderType.LIMIT;
        this.price = price;
        this.initialQuantity = quantity;
        this.quantity = quantity;
        this.executedQuantity = 0;
        this.status = OrderStatus.NEW;
        this.timestamp = timestamp;
    }

    public Order(String orderId,
                 String symbol,
                 OrderSide side,
                 double price,
                 long quantity,
                 long timestamp) {
        this(orderId, symbol, side, OrderType.LIMIT, price, quantity, timestamp);
    }

    public void reduceQuantity(long qty) {
        this.quantity -= qty;
        this.executedQuantity += qty;
        if (this.quantity == 0) {
            this.status = OrderStatus.FILLED;
        } else {
            this.status = OrderStatus.PARTIALLY_FILLED;
        }
    }

    public void cancel() {
        this.quantity = 0;
        this.status = OrderStatus.CANCELLED;
    }

    public String getOrderId() { return orderId; }
    public String getSymbol() { return symbol; }
    public OrderSide getSide() { return side; }
    public OrderType getType() { return type; }
    public double getPrice() { return price; }
    public long getInitialQuantity() { return initialQuantity; }
    public long getQuantity() { return quantity; }
    public long getExecutedQuantity() { return executedQuantity; }
    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus status) { this.status = status; }
    public long getTimestamp() { return timestamp; }
}
