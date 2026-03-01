package com.apex.exchange.load;

import com.apex.exchange.engine.kafka.OrderProducer;
import com.apex.exchange.engine.model.OrderEvent;
import com.apex.exchange.engine.model.OrderSide;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class LoadTestRunner implements CommandLineRunner {

    private final OrderProducer orderProducer;

    public LoadTestRunner(OrderProducer orderProducer) {
        this.orderProducer = orderProducer;
    }

    @Override
    public void run(String... args) {

        int totalOrders = 10000;

        long start = System.currentTimeMillis();

        for (int i = 0; i < totalOrders; i++) {
            OrderEvent sell = new OrderEvent(
                    "SELL-1",
                    "AAPL",
                    OrderSide.SELL,
                    100,
                    1000,
                    System.nanoTime()
            );

            OrderEvent buy = new OrderEvent(
                    "BUY-1",
                    "AAPL",
                    OrderSide.BUY,
                    100,
                    1000,
                    System.nanoTime()
            );

            orderProducer.publishOrder(sell);
            orderProducer.publishOrder(buy);
        }

        long end = System.currentTimeMillis();

        System.out.println("Submitted " + totalOrders + " orders in "
                + (end - start) + " ms");
    }
}