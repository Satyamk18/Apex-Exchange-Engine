package com.apex.exchange.engine.service;

import com.apex.exchange.engine.core.OrderBook;
import com.apex.exchange.engine.model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MatchingEngine {

    private final OrderBook orderBook = new OrderBook();

    public List<Trade> process(Order order) {
        return process(order, null);
    }

    public List<Trade> process(Order order, OrderStatusTracker tracker) {
        if (tracker != null) {
            tracker.registerOrder(order);
        }

        List<Trade> trades;
        if (order.getSide() == OrderSide.BUY) {
            trades = matchBuy(order, tracker);
        } else {
            trades = matchSell(order, tracker);
        }

        return trades;
    }

    public boolean cancelOrder(String orderId, OrderStatusTracker tracker) {
        boolean cancelled = orderBook.cancelOrder(orderId);
        if (cancelled && tracker != null) {
            tracker.markCancelled(orderId);
        }
        return cancelled;
    }

    private List<Trade> matchBuy(Order buyOrder, OrderStatusTracker tracker) {
        List<Trade> trades = new ArrayList<>();

        while (!orderBook.getSellOrders().isEmpty() && buyOrder.getQuantity() > 0) {

            Order bestSell = orderBook.getSellOrders().peek();

            boolean canMatch = (buyOrder.getType() == OrderType.MARKET)
                    || (buyOrder.getPrice() >= bestSell.getPrice());

            if (canMatch) {

                long tradedQty = Math.min(buyOrder.getQuantity(), bestSell.getQuantity());
                double tradePrice = bestSell.getPrice();

                buyOrder.reduceQuantity(tradedQty);
                bestSell.reduceQuantity(tradedQty);

                if (tracker != null) {
                    tracker.updateOrder(buyOrder);
                    tracker.updateOrder(bestSell);
                }

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
                    orderBook.removeActiveOrder(bestSell.getOrderId());
                }

            } else {
                break;
            }
        }

        if (buyOrder.getQuantity() > 0 && buyOrder.getType() == OrderType.LIMIT) {
            orderBook.addBuyOrder(buyOrder);
            if (tracker != null) {
                tracker.updateOrder(buyOrder);
            }
        } else if (buyOrder.getType() == OrderType.MARKET && buyOrder.getQuantity() > 0) {
            if (tracker != null) {
                tracker.updateOrder(buyOrder);
            }
        }

        return trades;
    }

    private List<Trade> matchSell(Order sellOrder, OrderStatusTracker tracker) {
        List<Trade> trades = new ArrayList<>();

        while (!orderBook.getBuyOrders().isEmpty() && sellOrder.getQuantity() > 0) {

            Order bestBuy = orderBook.getBuyOrders().peek();

            boolean canMatch = (sellOrder.getType() == OrderType.MARKET)
                    || (sellOrder.getPrice() <= bestBuy.getPrice());

            if (canMatch) {

                long tradedQty = Math.min(sellOrder.getQuantity(), bestBuy.getQuantity());
                double tradePrice = bestBuy.getPrice();

                sellOrder.reduceQuantity(tradedQty);
                bestBuy.reduceQuantity(tradedQty);

                if (tracker != null) {
                    tracker.updateOrder(sellOrder);
                    tracker.updateOrder(bestBuy);
                }

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
                    orderBook.removeActiveOrder(bestBuy.getOrderId());
                }

            } else {
                break;
            }
        }

        if (sellOrder.getQuantity() > 0 && sellOrder.getType() == OrderType.LIMIT) {
            orderBook.addSellOrder(sellOrder);
            if (tracker != null) {
                tracker.updateOrder(sellOrder);
            }
        } else if (sellOrder.getType() == OrderType.MARKET && sellOrder.getQuantity() > 0) {
            if (tracker != null) {
                tracker.updateOrder(sellOrder);
            }
        }

        return trades;
    }

    public OrderBook getOrderBook() {
        return orderBook;
    }
}