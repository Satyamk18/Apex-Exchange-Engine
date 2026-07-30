package com.apex.exchange.engine.service;

import com.apex.exchange.engine.model.*;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class SymbolEngine {

    private final String symbol;
    private final MatchingEngine matchingEngine;
    private final TradePublisher tradePublisher;
    private final OrderStatusTracker orderStatusTracker;
    private final BlockingQueue<OrderEvent> queue;

    public SymbolEngine(String symbol,
                        MatchingEngine matchingEngine,
                        TradePublisher tradePublisher,
                        OrderStatusTracker orderStatusTracker) {
        this.symbol = symbol;
        this.matchingEngine = matchingEngine;
        this.tradePublisher = tradePublisher;
        this.orderStatusTracker = orderStatusTracker;
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
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        thread.setName("engine-" + symbol);
        thread.start();
    }

    private void publishTrades(List<Trade> trades) {
        for (Trade trade : trades) {
            tradePublisher.publish(trade);
        }
    }
}
