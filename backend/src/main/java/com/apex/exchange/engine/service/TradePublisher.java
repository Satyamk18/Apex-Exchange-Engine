package com.apex.exchange.engine.service;

import com.apex.exchange.engine.model.Trade;

public interface TradePublisher {

    void publish(Trade trade);
}
