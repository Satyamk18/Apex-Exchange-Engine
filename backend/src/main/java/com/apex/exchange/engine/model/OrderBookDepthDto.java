package com.apex.exchange.engine.model;

import java.util.List;

public class OrderBookDepthDto {

    private String symbol;
    private long timestamp;
    private List<PriceLevelDto> bids;
    private List<PriceLevelDto> asks;

    public OrderBookDepthDto() {}

    public OrderBookDepthDto(String symbol, long timestamp, List<PriceLevelDto> bids, List<PriceLevelDto> asks) {
        this.symbol = symbol;
        this.timestamp = timestamp;
        this.bids = bids;
        this.asks = asks;
    }

    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public List<PriceLevelDto> getBids() { return bids; }
    public void setBids(List<PriceLevelDto> bids) { this.bids = bids; }

    public List<PriceLevelDto> getAsks() { return asks; }
    public void setAsks(List<PriceLevelDto> asks) { this.asks = asks; }
}
