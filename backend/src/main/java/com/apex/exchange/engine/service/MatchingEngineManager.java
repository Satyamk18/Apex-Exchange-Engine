package com.apex.exchange.engine.service;

import com.apex.exchange.engine.model.Order;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class MatchingEngineManager {

    private final ConcurrentHashMap<String, SymbolEngine> engines = new ConcurrentHashMap<>();

    public void submitOrder(Order order) {
        engines
                .computeIfAbsent(order.getSymbol(), symbol ->
                        new SymbolEngine(symbol, new MatchingEngine())
                )
                .submit(order);
    }

    public Collection<SymbolEngine> getAllEngines() {
        return engines.values();
    }
}