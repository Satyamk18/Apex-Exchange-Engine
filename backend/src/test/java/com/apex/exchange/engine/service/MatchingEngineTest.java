package com.apex.exchange.engine.service;

import com.apex.exchange.engine.model.Order;
import com.apex.exchange.engine.model.OrderSide;
import com.apex.exchange.engine.model.OrderType;
import com.apex.exchange.engine.model.Trade;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MatchingEngineTest {

    private MatchingEngine matchingEngine;

    @BeforeEach
    void setUp() {
        matchingEngine = new MatchingEngine();
    }

    @Test
    void testLimitOrderRestingInBookWhenNoMatch() {
        Order limitBuy = new Order("b1", "AAPL", OrderSide.BUY, OrderType.LIMIT, 150.0, 10, System.nanoTime());
        List<Trade> trades = matchingEngine.process(limitBuy);

        assertTrue(trades.isEmpty());
        assertEquals(1, matchingEngine.getOrderBook().getBuyOrders().size());
        assertEquals(10, matchingEngine.getOrderBook().getBuyOrders().peek().getQuantity());
    }

    @Test
    void testLimitOrdersMatch() {
        Order limitSell = new Order("s1", "AAPL", OrderSide.SELL, OrderType.LIMIT, 150.0, 10, System.nanoTime());
        matchingEngine.process(limitSell);

        Order limitBuy = new Order("b1", "AAPL", OrderSide.BUY, OrderType.LIMIT, 155.0, 10, System.nanoTime());
        List<Trade> trades = matchingEngine.process(limitBuy);

        assertEquals(1, trades.size());
        Trade trade = trades.get(0);
        assertEquals(150.0, trade.getPrice());
        assertEquals(10, trade.getQuantity());
        assertEquals("b1", trade.getBuyOrderId());
        assertEquals("s1", trade.getSellOrderId());
        assertTrue(matchingEngine.getOrderBook().getSellOrders().isEmpty());
        assertTrue(matchingEngine.getOrderBook().getBuyOrders().isEmpty());
    }

    @Test
    void testMarketBuyOrderFillsAgainstRestingLimitSells() {
        Order sell1 = new Order("s1", "AAPL", OrderSide.SELL, OrderType.LIMIT, 150.0, 10, System.nanoTime());
        Order sell2 = new Order("s2", "AAPL", OrderSide.SELL, OrderType.LIMIT, 152.0, 20, System.nanoTime());
        matchingEngine.process(sell1);
        matchingEngine.process(sell2);

        Order marketBuy = new Order("b1", "AAPL", OrderSide.BUY, OrderType.MARKET, 0.0, 25, System.nanoTime());
        List<Trade> trades = matchingEngine.process(marketBuy);

        assertEquals(2, trades.size());
        assertEquals(150.0, trades.get(0).getPrice());
        assertEquals(10, trades.get(0).getQuantity());

        assertEquals(152.0, trades.get(1).getPrice());
        assertEquals(15, trades.get(1).getQuantity());

        assertEquals(1, matchingEngine.getOrderBook().getSellOrders().size());
        assertEquals(5, matchingEngine.getOrderBook().getSellOrders().peek().getQuantity());
        assertTrue(matchingEngine.getOrderBook().getBuyOrders().isEmpty());
    }

    @Test
    void testMarketOrderUnfilledQuantityIsNotAddedToBook() {
        Order sell1 = new Order("s1", "AAPL", OrderSide.SELL, OrderType.LIMIT, 100.0, 5, System.nanoTime());
        matchingEngine.process(sell1);

        Order marketBuy = new Order("b1", "AAPL", OrderSide.BUY, OrderType.MARKET, 0.0, 20, System.nanoTime());
        List<Trade> trades = matchingEngine.process(marketBuy);

        assertEquals(1, trades.size());
        assertEquals(5, trades.get(0).getQuantity());

        assertTrue(matchingEngine.getOrderBook().getSellOrders().isEmpty());
        assertTrue(matchingEngine.getOrderBook().getBuyOrders().isEmpty());
    }

    @Test
    void testMarketOrderOnEmptyBookProducesNoTradesAndDoesNotEnterBook() {
        Order marketSell = new Order("s1", "AAPL", OrderSide.SELL, OrderType.MARKET, 0.0, 100, System.nanoTime());
        List<Trade> trades = matchingEngine.process(marketSell);

        assertTrue(trades.isEmpty());
        assertTrue(matchingEngine.getOrderBook().getBuyOrders().isEmpty());
        assertTrue(matchingEngine.getOrderBook().getSellOrders().isEmpty());
    }
}
