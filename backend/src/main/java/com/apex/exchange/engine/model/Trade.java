package com.apex.exchange.engine.model;

public class Trade {

    private final String tradeId;
    private final String symbol;
    private final double price;
    private final long quantity;
    private final String buyOrderId;
    private final String sellOrderId;

    public Trade(String tradeId,
                 String symbol,
                 double price,
                 long quantity,
                 String buyOrderId,
                 String sellOrderId) {
        this.tradeId = tradeId;
        this.symbol = symbol;
        this.price = price;
        this.quantity = quantity;
        this.buyOrderId = buyOrderId;
        this.sellOrderId = sellOrderId;
    }

    public String getTradeId() { return tradeId; }
    public String getSymbol() { return symbol; }
    public double getPrice() { return price; }
    public long getQuantity() { return quantity; }
    public String getBuyOrderId() { return buyOrderId; }
    public String getSellOrderId() { return sellOrderId; }
}