package com.apex.exchange.engine.model;

public class OrderEvent {

    private String orderId;
    private String symbol;
    private OrderSide side;
    private OrderType type;
    private OrderAction action = OrderAction.CREATE;
    private double price;
    private long quantity;
    private long timestamp;

    public OrderEvent() {}

    public OrderEvent(String orderId,
                      String symbol,
                      OrderSide side,
                      OrderType type,
                      OrderAction action,
                      double price,
                      long quantity,
                      long timestamp) {
        this.orderId = orderId;
        this.symbol = symbol;
        this.side = side;
        this.type = type != null ? type : OrderType.LIMIT;
        this.action = action != null ? action : OrderAction.CREATE;
        this.price = price;
        this.quantity = quantity;
        this.timestamp = timestamp;
    }

    public OrderEvent(String orderId,
                      String symbol,
                      OrderSide side,
                      OrderType type,
                      double price,
                      long quantity,
                      long timestamp) {
        this(orderId, symbol, side, type, OrderAction.CREATE, price, quantity, timestamp);
    }

    public OrderEvent(String orderId,
                      String symbol,
                      OrderSide side,
                      double price,
                      long quantity,
                      long timestamp) {
        this(orderId, symbol, side, OrderType.LIMIT, OrderAction.CREATE, price, quantity, timestamp);
    }

    public String getOrderId() { return orderId; }
    public String getSymbol() { return symbol; }
    public OrderSide getSide() { return side; }
    public OrderType getType() { return type; }
    public OrderAction getAction() { return action; }
    public double getPrice() { return price; }
    public long getQuantity() { return quantity; }
    public long getTimestamp() { return timestamp; }

    public void setOrderId(String orderId) { this.orderId = orderId; }
    public void setSymbol(String symbol) { this.symbol = symbol; }
    public void setSide(OrderSide side) { this.side = side; }
    public void setType(OrderType type) { this.type = type; }
    public void setAction(OrderAction action) { this.action = action; }
    public void setPrice(double price) { this.price = price; }
    public void setQuantity(long quantity) { this.quantity = quantity; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}