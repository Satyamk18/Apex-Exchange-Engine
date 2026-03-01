package com.apex.exchange.engine.model;

public class Order {

    private final String orderId;
    private final String symbol;
    private final OrderSide side;
    private final double price;
    private long quantity;
    private final long timestamp;

    public Order(String orderId,
                 String symbol,
                 OrderSide side,
                 double price,
                 long quantity,
                 long timestamp) {
        this.orderId = orderId;
        this.symbol = symbol;
        this.side = side;
        this.price = price;
        this.quantity = quantity;
        this.timestamp = timestamp;
    }

    public void reduceQuantity(long qty) {
        this.quantity -= qty;
    }

    public String getOrderId() { return orderId; }
    public String getSymbol() { return symbol; }
    public OrderSide getSide() { return side; }
    public double getPrice() { return price; }
    public long getQuantity() { return quantity; }
    public long getTimestamp() { return timestamp; }
}
