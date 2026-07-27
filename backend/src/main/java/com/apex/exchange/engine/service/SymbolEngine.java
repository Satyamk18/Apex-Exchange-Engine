package com.apex.exchange.engine.service;

import com.apex.exchange.engine.model.Order;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class SymbolEngine {

    private final String symbol;
    private final MatchingEngine matchingEngine;
    private final BlockingQueue<Order> queue;

    public SymbolEngine(String symbol, MatchingEngine matchingEngine) {
        this.symbol = symbol;
        this.matchingEngine = matchingEngine;
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
                    matchingEngine.process(order); // pure logic
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        thread.setName("engine-" + symbol);
        thread.start();
    }
}