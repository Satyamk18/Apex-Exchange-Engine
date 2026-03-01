package com.apex.exchange.engine.model;

public class TradeResult {

    private final String buyOrderId;
    private final String sellOrderId;
    private final double price;
    private final long quantity;

    public TradeResult(String buyOrderId, String sellOrderId, double price, long quantity) {
        this.buyOrderId = buyOrderId;
        this.sellOrderId = sellOrderId;
        this.price = price;
        this.quantity = quantity;
    }

    public String getBuyOrderId() {
        return buyOrderId;
    }

    public String getSellOrderId() {
        return sellOrderId;
    }

    public double getPrice() {
        return price;
    }

    public long getQuantity() {
        return quantity;
    }
}