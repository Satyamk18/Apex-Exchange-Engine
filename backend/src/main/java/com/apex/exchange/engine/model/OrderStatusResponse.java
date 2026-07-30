package com.apex.exchange.engine.model;

public class OrderStatusResponse {

    private String orderId;
    private String symbol;
    private OrderSide side;
    private OrderType type;
    private double price;
    private long initialQuantity;
    private long executedQuantity;
    private long remainingQuantity;
    private OrderStatus status;
    private long timestamp;

    public OrderStatusResponse() {}

    public OrderStatusResponse(String orderId,
                               String symbol,
                               OrderSide side,
                               OrderType type,
                               double price,
                               long initialQuantity,
                               long executedQuantity,
                               long remainingQuantity,
                               OrderStatus status,
                               long timestamp) {
        this.orderId = orderId;
        this.symbol = symbol;
        this.side = side;
        this.type = type;
        this.price = price;
        this.initialQuantity = initialQuantity;
        this.executedQuantity = executedQuantity;
        this.remainingQuantity = remainingQuantity;
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

    public long getRemainingQuantity() { return remainingQuantity; }
    public void setRemainingQuantity(long remainingQuantity) { this.remainingQuantity = remainingQuantity; }

    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus status) { this.status = status; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}
