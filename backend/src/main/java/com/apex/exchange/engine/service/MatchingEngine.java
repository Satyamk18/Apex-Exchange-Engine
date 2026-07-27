package com.apex.exchange.engine.service;

import com.apex.exchange.engine.core.OrderBook;
import com.apex.exchange.engine.model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MatchingEngine {

    private final OrderBook orderBook = new OrderBook();

    public List<Trade> process(Order order) {
        if (order.getSide() == OrderSide.BUY) {
            return matchBuy(order);
        } else {
            return matchSell(order);
        }
    }

    private List<Trade> matchBuy(Order buyOrder) {
        List<Trade> trades = new ArrayList<>();

        while (!orderBook.getSellOrders().isEmpty() && buyOrder.getQuantity() > 0) {

            Order bestSell = orderBook.getSellOrders().peek();

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
                        bestSell.getOrderId(),
                        System.nanoTime()
                ));

                if (bestSell.getQuantity() == 0) {
                    orderBook.getSellOrders().poll();
                }

            } else break;
        }

        if (buyOrder.getQuantity() > 0) {
            orderBook.getBuyOrders().offer(buyOrder);
        }

        return trades;
    }

    private List<Trade> matchSell(Order sellOrder) {
        List<Trade> trades = new ArrayList<>();

        while (!orderBook.getBuyOrders().isEmpty() && sellOrder.getQuantity() > 0) {

            Order bestBuy = orderBook.getBuyOrders().peek();

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
                        sellOrder.getOrderId(),
                        System.nanoTime()
                ));

                if (bestBuy.getQuantity() == 0) {
                    orderBook.getBuyOrders().poll();
                }

            } else break;
        }

        if (sellOrder.getQuantity() > 0) {
            orderBook.getSellOrders().offer(sellOrder);
        }

        return trades;
    }
}