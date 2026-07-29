package com.apex.exchange.engine.service;

import com.apex.exchange.engine.model.Order;
import com.apex.exchange.engine.model.Trade;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class SymbolEngine {

    private final String symbol;
    private final MatchingEngine matchingEngine;
    private final TradePublisher tradePublisher;
    private final BlockingQueue<Order> queue;

    public SymbolEngine(String symbol, MatchingEngine matchingEngine, TradePublisher tradePublisher) {
        this.symbol = symbol;
        this.matchingEngine = matchingEngine;
        this.tradePublisher = tradePublisher;
        this.queue = new LinkedBlockingQueue<>();

        start();
    }

    public void submit(Order order) {
        queue.offer(order);
    }

    private void start() {
        Thread thread = new Thread(() -> {
            while (true) {
                try {
                    Order order = queue.take();
                    List<Trade> trades = matchingEngine.process(order);
                    publishTrades(trades);
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
