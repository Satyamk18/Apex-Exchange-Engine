package com.apex.exchange;

import com.apex.exchange.engine.model.Order;
import com.apex.exchange.engine.model.OrderSide;
import com.apex.exchange.engine.service.MatchingEngine;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BackendApplication {

    public static void main(String[] args) {
        MatchingEngine engine = new MatchingEngine();

        Order sell = new Order("S1", "AAPL",
                OrderSide.SELL, 100, 10,
                System.nanoTime());

        Order buy = new Order("B1", "AAPL",
                OrderSide.BUY, 105, 5,
                System.nanoTime());

        engine.processOrder(sell);
        var trades = engine.processOrder(buy);

        trades.forEach(t ->
                System.out.println("Trade executed: "
                        + t.getQuantity()
                        + " @ " + t.getPrice()));
    }

}
