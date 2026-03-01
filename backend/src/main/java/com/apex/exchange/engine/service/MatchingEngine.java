package com.apex.exchange.engine.service;

import com.apex.exchange.engine.core.OrderBook;
import com.apex.exchange.engine.kafka.TradeProducer;
import com.apex.exchange.engine.model.Order;
import com.apex.exchange.engine.model.OrderSide;
import com.apex.exchange.engine.model.TradeEvent;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

@Component
public class MatchingEngine {

    private final Map<String, OrderBook> orderBooks = new ConcurrentHashMap<>();
    private final Map<String, ReentrantLock> symbolLocks = new ConcurrentHashMap<>();

    private final TradeProducer tradeProducer;
    private final Counter orderCounter;
    private final Counter tradeCounter;
    private final Timer matchingTimer;

    public MatchingEngine(TradeProducer tradeProducer,
                          MeterRegistry meterRegistry) {
        this.tradeProducer = tradeProducer;

        this.orderCounter = meterRegistry.counter("exchange.orders.processed");
        this.tradeCounter = meterRegistry.counter("exchange.trades.executed");
        this.matchingTimer = meterRegistry.timer("exchange.matching.latency");
    }

    public void match(Order order) {

        String symbol = order.getSymbol();

        orderBooks.putIfAbsent(symbol, new OrderBook());
        symbolLocks.putIfAbsent(symbol, new ReentrantLock());

        OrderBook book = orderBooks.get(symbol);
        ReentrantLock lock = symbolLocks.get(symbol);

        long startTime = System.nanoTime();
        lock.lock();
        try {

            if (order.getSide() == OrderSide.BUY) {
                processBuyOrder(order, book);
            } else {
                processSellOrder(order, book);
            }

            orderCounter.increment();

        } finally {
            lock.unlock();
            long duration = System.nanoTime() - startTime;
            matchingTimer.record(duration, TimeUnit.NANOSECONDS);
        }
    }

    private void processBuyOrder(Order buyOrder, OrderBook book) {

        while (!book.getSellBook().isEmpty()
                && buyOrder.getQuantity() > 0
                && book.getSellBook().peek().getPrice() <= buyOrder.getPrice()) {

            Order bestSell = book.getSellBook().peek();

            long tradedQty = Math.min(buyOrder.getQuantity(), bestSell.getQuantity());
            double tradePrice = bestSell.getPrice();

            buyOrder.reduceQuantity(tradedQty);
            bestSell.reduceQuantity(tradedQty);

            if (bestSell.getQuantity() == 0) {
                book.getSellBook().poll();
            }

            publishTrade(
                    buyOrder.getSymbol(),
                    buyOrder.getOrderId(),
                    bestSell.getOrderId(),
                    tradePrice,
                    tradedQty
            );
        }

        if (buyOrder.getQuantity() > 0) {
            book.getBuyBook().add(buyOrder);
        }
    }

    private void processSellOrder(Order sellOrder, OrderBook book) {

        while (!book.getBuyBook().isEmpty()
                && sellOrder.getQuantity() > 0
                && book.getBuyBook().peek().getPrice() >= sellOrder.getPrice()) {

            Order bestBuy = book.getBuyBook().peek();

            long tradedQty = Math.min(sellOrder.getQuantity(), bestBuy.getQuantity());
            double tradePrice = bestBuy.getPrice();

            sellOrder.reduceQuantity(tradedQty);
            bestBuy.reduceQuantity(tradedQty);

            if (bestBuy.getQuantity() == 0) {
                book.getBuyBook().poll();
            }

            publishTrade(
                    sellOrder.getSymbol(),
                    bestBuy.getOrderId(),
                    sellOrder.getOrderId(),
                    tradePrice,
                    tradedQty
            );
        }

        if (sellOrder.getQuantity() > 0) {
            book.getSellBook().add(sellOrder);
        }
    }

    private void publishTrade(String symbol,
                              String buyOrderId,
                              String sellOrderId,
                              double price,
                              long quantity) {

        TradeEvent event = new TradeEvent(
                symbol,
                buyOrderId,
                sellOrderId,
                price,
                quantity,
                System.nanoTime()
        );

        tradeProducer.publishTrade(event);
        tradeCounter.increment();
    }
}