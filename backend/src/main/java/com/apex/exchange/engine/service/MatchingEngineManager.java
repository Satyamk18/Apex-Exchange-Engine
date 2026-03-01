package com.apex.exchange.engine.service;

import com.apex.exchange.engine.model.Order;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;

@Service
public class MatchingEngineManager {

    private final ConcurrentHashMap<String, MatchingEngine> engines = new ConcurrentHashMap<>();
    private final MeterRegistry registry;

    public MatchingEngineManager(MeterRegistry registry) {
        this.registry = registry;
    }

    private MatchingEngine createEngine() {
        return new MatchingEngine(registry);
    }

    public MatchingEngine getEngine(String symbol) {
        return engines.computeIfAbsent(symbol, s -> createEngine());
    }

    public void submitOrder(Order order) {
        MatchingEngine engine = getEngine(order.getSymbol());
        engine.match(order);
    }

    public ConcurrentHashMap<String, MatchingEngine> getAllEngines() {
        return engines;
    }
}