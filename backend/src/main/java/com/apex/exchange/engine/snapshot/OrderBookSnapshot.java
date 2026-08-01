package com.apex.exchange.engine.snapshot;

import com.apex.exchange.engine.model.Order;

import java.io.Serializable;
import java.util.List;

public class OrderBookSnapshot implements Serializable {

    private static final long serialVersionUID = 1L;

    private String symbol;
    private long timestamp;
    private long snapshotSequence;
    private List<Order> buyOrders;
    private List<Order> sellOrders;

    public OrderBookSnapshot() {}

    public OrderBookSnapshot(String symbol,
                             long timestamp,
                             long snapshotSequence,
                             List<Order> buyOrders,
                             List<Order> sellOrders) {
        this.symbol = symbol;
        this.timestamp = timestamp;
        this.snapshotSequence = snapshotSequence;
        this.buyOrders = buyOrders;
        this.sellOrders = sellOrders;
    }

    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public long getSnapshotSequence() { return snapshotSequence; }
    public void setSnapshotSequence(long snapshotSequence) { this.snapshotSequence = snapshotSequence; }

    public List<Order> getBuyOrders() { return buyOrders; }
    public void setBuyOrders(List<Order> buyOrders) { this.buyOrders = buyOrders; }

    public List<Order> getSellOrders() { return sellOrders; }
    public void setSellOrders(List<Order> sellOrders) { this.sellOrders = sellOrders; }
}
