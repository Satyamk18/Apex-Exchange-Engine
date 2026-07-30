package com.apex.exchange.engine.service;

import com.apex.exchange.engine.model.*;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class MatchingEngineManager {

    private final ConcurrentHashMap<String, SymbolEngine> engines = new ConcurrentHashMap<>();
    private final TradePublisher tradePublisher;
    private final OrderStatusTracker orderStatusTracker;

    public MatchingEngineManager(TradePublisher tradePublisher, OrderStatusTracker orderStatusTracker) {
        this.tradePublisher = tradePublisher;
        this.orderStatusTracker = orderStatusTracker;
    }

    public void processEvent(OrderEvent event) {
        engines
                .computeIfAbsent(event.getSymbol(), symbol ->
                        new SymbolEngine(symbol, new MatchingEngine(), tradePublisher, orderStatusTracker)
                )
                .submit(event);
    }

    public void submitOrder(Order order) {
        OrderEvent event = new OrderEvent(
                order.getOrderId(),
                order.getSymbol(),
                order.getSide(),
                order.getType(),
                OrderAction.CREATE,
                order.getPrice(),
                order.getQuantity(),
                order.getTimestamp()
        );
        processEvent(event);
    }

    public OrderBookDepthDto getOrderBookDepth(String symbol, int depth) {
        SymbolEngine symbolEngine = engines.get(symbol);
        if (symbolEngine == null) {
            return new OrderBookDepthDto(symbol, System.currentTimeMillis(), Collections.emptyList(), Collections.emptyList());
        }
        return symbolEngine.getMatchingEngine().getOrderBook().getDepth(symbol, depth);
    }

    public Collection<SymbolEngine> getAllEngines() {
        return engines.values();
    }
}
