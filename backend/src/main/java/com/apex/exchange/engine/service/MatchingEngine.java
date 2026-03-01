package com.apex.exchange.engine.service;

import com.apex.exchange.engine.model.Order;
import com.apex.exchange.engine.model.OrderSide;
import com.apex.exchange.engine.model.Trade;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

public class MatchingEngine {

    private final PriorityQueue<Order> buyBook;
    private final PriorityQueue<Order> sellBook;

    private final ReentrantLock lock = new ReentrantLock();

    private final Counter ordersProcessed;
    private final Counter tradesExecuted;
    private final Timer matchingLatency;

    public MatchingEngine(MeterRegistry registry) {

        this.buyBook = new PriorityQueue<>(
                Comparator.comparingDouble(Order::getPrice).reversed()
                        .thenComparingLong(Order::getTimestamp)
        );

        this.sellBook = new PriorityQueue<>(
                Comparator.comparingDouble(Order::getPrice)
                        .thenComparingLong(Order::getTimestamp)
        );

        this.ordersProcessed = registry.counter("exchange.orders.processed");
        this.tradesExecuted = registry.counter("exchange.trades.executed");
        this.matchingLatency = registry.timer("exchange.matching.latency");
    }

    public List<Trade> match(Order incomingOrder) {

        return matchingLatency.record(() -> {

            lock.lock();
            try {
                ordersProcessed.increment();
                List<Trade> trades = new ArrayList<>();

                if (incomingOrder.getSide() == OrderSide.BUY) {
                    matchBuy(incomingOrder, trades);
                } else {
                    matchSell(incomingOrder, trades);
                }

                return trades;

            } finally {
                lock.unlock();
            }
        });
    }

    private void matchBuy(Order buyOrder, List<Trade> trades) {

        while (!sellBook.isEmpty() && buyOrder.getQuantity() > 0) {

            Order bestSell = sellBook.peek();

            if (buyOrder.getPrice() >= bestSell.getPrice()) {

                long tradedQty = Math.min(buyOrder.getQuantity(), bestSell.getQuantity());
                double tradePrice = bestSell.getPrice();

                buyOrder.reduceQuantity(tradedQty);
                bestSell.reduceQuantity(tradedQty);

                trades.add(new Trade(
                        UUID.randomUUID().toString(),
                        buyOrder.getSymbol(),
                        tradePrice,
                        tradedQty,
                        buyOrder.getOrderId(),
                        bestSell.getOrderId()
                ));

                tradesExecuted.increment();

                if (bestSell.getQuantity() == 0) {
                    sellBook.poll();
                }

            } else {
                break;
            }
        }

        if (buyOrder.getQuantity() > 0) {
            buyBook.add(buyOrder);
        }
    }

    private void matchSell(Order sellOrder, List<Trade> trades) {

        while (!buyBook.isEmpty() && sellOrder.getQuantity() > 0) {

            Order bestBuy = buyBook.peek();

            if (sellOrder.getPrice() <= bestBuy.getPrice()) {

                long tradedQty = Math.min(sellOrder.getQuantity(), bestBuy.getQuantity());
                double tradePrice = bestBuy.getPrice();

                sellOrder.reduceQuantity(tradedQty);
                bestBuy.reduceQuantity(tradedQty);

                trades.add(new Trade(
                        UUID.randomUUID().toString(),
                        sellOrder.getSymbol(),
                        tradePrice,
                        tradedQty,
                        bestBuy.getOrderId(),
                        sellOrder.getOrderId()
                ));

                tradesExecuted.increment();

                if (bestBuy.getQuantity() == 0) {
                    buyBook.poll();
                }

            } else {
                break;
            }
        }

        if (sellOrder.getQuantity() > 0) {
            sellBook.add(sellOrder);
        }
    }

    public String snapshot() {
        return "BUY_BOOK=" + buyBook +
                "\nSELL_BOOK=" + sellBook;
    }
}