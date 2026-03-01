package com.apex.exchange.engine.service;

import com.apex.exchange.engine.core.OrderBook;
import com.apex.exchange.engine.model.Order;
import com.apex.exchange.engine.model.OrderSide;
import com.apex.exchange.engine.model.Trade;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class MatchingEngine {

    private final Map<String, OrderBook> books = new ConcurrentHashMap<>();

    public List<Trade> processOrder(Order order) {

        books.putIfAbsent(order.getSymbol(), new OrderBook());
        OrderBook book = books.get(order.getSymbol());

        List<Trade> trades = new ArrayList<>();

        if (order.getSide() == OrderSide.BUY) {
            matchBuy(order, book, trades);
        } else {
            matchSell(order, book, trades);
        }

        if (order.getQuantity() > 0) {
            book.addOrder(order);
        }

        return trades;
    }

    private void matchBuy(Order buyOrder,
                          OrderBook book,
                          List<Trade> trades) {

        while (!book.getSellOrders().isEmpty()
                && buyOrder.getQuantity() > 0
                && book.getSellOrders().peek().getPrice() <= buyOrder.getPrice()) {

            Order sellOrder = book.getSellOrders().peek();

            long tradedQty = Math.min(buyOrder.getQuantity(), sellOrder.getQuantity());

            trades.add(new Trade(
                    UUID.randomUUID().toString(),
                    buyOrder.getSymbol(),
                    sellOrder.getPrice(),
                    tradedQty,
                    buyOrder.getOrderId(),
                    sellOrder.getOrderId()
            ));

            buyOrder.reduceQuantity(tradedQty);
            sellOrder.reduceQuantity(tradedQty);

            if (sellOrder.getQuantity() == 0) {
                book.getSellOrders().poll();
            }
        }
    }

    private void matchSell(Order sellOrder,
                           OrderBook book,
                           List<Trade> trades) {

        while (!book.getBuyOrders().isEmpty()
                && sellOrder.getQuantity() > 0
                && book.getBuyOrders().peek().getPrice() >= sellOrder.getPrice()) {

            Order buyOrder = book.getBuyOrders().peek();

            long tradedQty = Math.min(sellOrder.getQuantity(), buyOrder.getQuantity());

            trades.add(new Trade(
                    UUID.randomUUID().toString(),
                    sellOrder.getSymbol(),
                    buyOrder.getPrice(),
                    tradedQty,
                    buyOrder.getOrderId(),
                    sellOrder.getOrderId()
            ));

            sellOrder.reduceQuantity(tradedQty);
            buyOrder.reduceQuantity(tradedQty);

            if (buyOrder.getQuantity() == 0) {
                book.getBuyOrders().poll();
            }
        }
    }
}