package com.apex.exchange.load;

import com.apex.exchange.engine.model.Order;
import com.apex.exchange.engine.model.OrderSide;
import com.apex.exchange.engine.model.OrderType;
import com.apex.exchange.engine.service.MatchingEngineManager;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Component
@Profile("load-test")
public class LoadTestRunner implements CommandLineRunner {

    private final MatchingEngineManager manager;

    public LoadTestRunner(MatchingEngineManager manager) {
        this.manager = manager;
    }

    @Override
    public void run(String... args) throws Exception {

        int threads = 8;
        int ordersPerThread = 10000;

        ExecutorService executor = Executors.newFixedThreadPool(threads);

        long start = System.nanoTime();

        for (int t = 0; t < threads; t++) {
            executor.submit(() -> {
                for (int i = 0; i < ordersPerThread; i++) {

                    Order order = new Order(
                            "id-" + Thread.currentThread().getId() + "-" + i,
                            "AAPL",
                            i % 2 == 0 ? OrderSide.BUY : OrderSide.SELL,
                            OrderType.LIMIT,
                            100,
                            10,
                            System.nanoTime()
                    );

                    manager.submitOrder(order);
                }
            });
        }

        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.MINUTES);

        long end = System.nanoTime();

        double seconds = (end - start) / 1_000_000_000.0;
        int totalOrders = threads * ordersPerThread;

        System.out.println("=================================");
        System.out.println("Total Orders: " + totalOrders);
        System.out.println("Time Taken (seconds): " + seconds);
        System.out.println("Throughput (orders/sec): " + (totalOrders / seconds));
        System.out.println("=================================");
    }
}
