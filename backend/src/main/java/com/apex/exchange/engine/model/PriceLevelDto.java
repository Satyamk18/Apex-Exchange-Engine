package com.apex.exchange.engine.model;

public class PriceLevelDto {

    private double price;
    private long quantity;
    private int orderCount;

    public PriceLevelDto() {}

    public PriceLevelDto(double price, long quantity, int orderCount) {
        this.price = price;
        this.quantity = quantity;
        this.orderCount = orderCount;
    }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public long getQuantity() { return quantity; }
    public void setQuantity(long quantity) { this.quantity = quantity; }

    public int getOrderCount() { return orderCount; }
    public void setOrderCount(int orderCount) { this.orderCount = orderCount; }
}
