package com.apex.exchange.engine.service;

import com.apex.exchange.engine.metrics.EngineLatencyMetrics;
import com.apex.exchange.engine.model.*;
import com.apex.exchange.engine.snapshot.SnapshotService;
import com.apex.exchange.engine.websocket.MarketDataBroadcaster;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class MatchingEngineManager {

    private final ConcurrentHashMap<String, SymbolEngine> engines = new ConcurrentHashMap<>();
    private final TradePublisher tradePublisher;
    private final OrderStatusTracker orderStatusTracker;
    private final SnapshotService snapshotService;
    private final MarketDataBroadcaster marketDataBroadcaster;
    private final EngineLatencyMetrics engineLatencyMetrics;

    public MatchingEngineManager(TradePublisher tradePublisher,
                                 OrderStatusTracker orderStatusTracker,
                                 SnapshotService snapshotService,
                                 MarketDataBroadcaster marketDataBroadcaster) {
        this(tradePublisher, orderStatusTracker, snapshotService, marketDataBroadcaster, null);
    }

    @Autowired
    public MatchingEngineManager(TradePublisher tradePublisher,
                                 OrderStatusTracker orderStatusTracker,
                                 SnapshotService snapshotService,
                                 MarketDataBroadcaster marketDataBroadcaster,
                                 EngineLatencyMetrics engineLatencyMetrics) {
        this.tradePublisher = tradePublisher;
        this.orderStatusTracker = orderStatusTracker;
        this.snapshotService = snapshotService;
        this.marketDataBroadcaster = marketDataBroadcaster;
        this.engineLatencyMetrics = engineLatencyMetrics;
    }

    public void processEvent(OrderEvent event) {
        getOrCreateSymbolEngine(event.getSymbol()).submit(event);
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

    /**
     * Returns the MatchingEngine for a given symbol, creating a new SymbolEngine if necessary.
     * Used by EngineRecoveryService to restore order book state before accepting live traffic.
     */
    public MatchingEngine getOrCreateEngine(String symbol) {
        return getOrCreateSymbolEngine(symbol).getMatchingEngine();
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

    private SymbolEngine getOrCreateSymbolEngine(String symbol) {
        return engines.computeIfAbsent(symbol, s ->
                new SymbolEngine(s, new MatchingEngine(), tradePublisher, orderStatusTracker, snapshotService, marketDataBroadcaster, engineLatencyMetrics)
        );
    }
}
