package com.apex.exchange.engine.model;

public class TradeEvent {

    private String tradeId;
    private String symbol;
    private String buyOrderId;
    private String sellOrderId;
    private double price;
    private long quantity;
    private long timestamp;

    public TradeEvent() {}

    public TradeEvent(String tradeId,
                      String symbol,
                      String buyOrderId,
                      String sellOrderId,
                      double price,
                      long quantity,
                      long timestamp) {
        this.tradeId = tradeId;
        this.symbol = symbol;
        this.buyOrderId = buyOrderId;
        this.sellOrderId = sellOrderId;
        this.price = price;
        this.quantity = quantity;
        this.timestamp = timestamp;
    }

    public String getTradeId() { return tradeId; }
    public String getSymbol() { return symbol; }
    public String getBuyOrderId() { return buyOrderId; }
    public String getSellOrderId() { return sellOrderId; }
    public double getPrice() { return price; }
    public long getQuantity() { return quantity; }
    public long getTimestamp() { return timestamp; }

    public void setTradeId(String tradeId) { this.tradeId = tradeId; }
    public void setSymbol(String symbol) { this.symbol = symbol; }
    public void setBuyOrderId(String buyOrderId) { this.buyOrderId = buyOrderId; }
    public void setSellOrderId(String sellOrderId) { this.sellOrderId = sellOrderId; }
    public void setPrice(double price) { this.price = price; }
    public void setQuantity(long quantity) { this.quantity = quantity; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}
