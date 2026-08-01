package com.apex.exchange.engine.service;

import com.apex.exchange.engine.model.*;
import com.apex.exchange.engine.snapshot.SnapshotService;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicLong;

public class SymbolEngine {

    private static final int SNAPSHOT_INTERVAL = 1000;

    private final String symbol;
    private final MatchingEngine matchingEngine;
    private final TradePublisher tradePublisher;
    private final OrderStatusTracker orderStatusTracker;
    private final SnapshotService snapshotService;
    private final BlockingQueue<OrderEvent> queue;
    private final AtomicLong processedCount = new AtomicLong(0);

    public SymbolEngine(String symbol,
                        MatchingEngine matchingEngine,
                        TradePublisher tradePublisher,
                        OrderStatusTracker orderStatusTracker,
                        SnapshotService snapshotService) {
        this.symbol = symbol;
        this.matchingEngine = matchingEngine;
        this.tradePublisher = tradePublisher;
        this.orderStatusTracker = orderStatusTracker;
        this.snapshotService = snapshotService;
        this.queue = new LinkedBlockingQueue<>();

        start();
    }

    public void submit(OrderEvent event) {
        queue.offer(event);
    }

    public MatchingEngine getMatchingEngine() {
        return matchingEngine;
    }

    private void start() {
        Thread thread = new Thread(() -> {
            while (true) {
                try {
                    OrderEvent event = queue.take();

                    if (event.getAction() == OrderAction.CANCEL) {
                        matchingEngine.cancelOrder(event.getOrderId(), orderStatusTracker);
                    } else {
                        Order order = new Order(
                                event.getOrderId(),
                                event.getSymbol(),
                                event.getSide(),
                                event.getType(),
                                event.getPrice(),
                                event.getQuantity(),
                                event.getTimestamp()
                        );
                        List<Trade> trades = matchingEngine.process(order, orderStatusTracker);
                        publishTrades(trades);
                    }

                    // Periodic snapshot after every SNAPSHOT_INTERVAL operations
                    long count = processedCount.incrementAndGet();
                    if (count % SNAPSHOT_INTERVAL == 0) {
                        snapshotService.saveSnapshot(symbol, matchingEngine, count);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        thread.setName("engine-" + symbol);
        thread.setDaemon(true);
        thread.start();
    }

    private void publishTrades(List<Trade> trades) {
        for (Trade trade : trades) {
            tradePublisher.publish(trade);
        }
    }
}
