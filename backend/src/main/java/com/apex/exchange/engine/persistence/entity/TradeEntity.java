package com.apex.exchange.engine.persistence.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "trades")
public class TradeEntity {

    @Id
    @Column(name = "trade_id", nullable = false, unique = true)
    private String tradeId;

    @Column(name = "symbol", nullable = false)
    private String symbol;

    @Column(name = "price", nullable = false)
    private double price;

    @Column(name = "quantity", nullable = false)
    private long quantity;

    @Column(name = "buy_order_id", nullable = false)
    private String buyOrderId;

    @Column(name = "sell_order_id", nullable = false)
    private String sellOrderId;

    @Column(name = "timestamp", nullable = false)
    private long timestamp;

    public TradeEntity() {}

    public TradeEntity(String tradeId, String symbol, double price, long quantity,
                       String buyOrderId, String sellOrderId, long timestamp) {
        this.tradeId = tradeId;
        this.symbol = symbol;
        this.price = price;
        this.quantity = quantity;
        this.buyOrderId = buyOrderId;
        this.sellOrderId = sellOrderId;
        this.timestamp = timestamp;
    }

    public String getTradeId() { return tradeId; }
    public void setTradeId(String tradeId) { this.tradeId = tradeId; }

    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public long getQuantity() { return quantity; }
    public void setQuantity(long quantity) { this.quantity = quantity; }

    public String getBuyOrderId() { return buyOrderId; }
    public void setBuyOrderId(String buyOrderId) { this.buyOrderId = buyOrderId; }

    public String getSellOrderId() { return sellOrderId; }
    public void setSellOrderId(String sellOrderId) { this.sellOrderId = sellOrderId; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}
