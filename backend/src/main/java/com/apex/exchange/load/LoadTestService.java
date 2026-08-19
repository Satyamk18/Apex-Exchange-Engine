package com.apex.exchange.load;

import com.apex.exchange.engine.model.Order;
import com.apex.exchange.engine.model.OrderSide;
import com.apex.exchange.engine.model.OrderType;
import com.apex.exchange.engine.service.MatchingEngineManager;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class LoadTestService {

    private static final Logger log = LoggerFactory.getLogger(LoadTestService.class);

    private final MatchingEngineManager manager;
    private final AtomicReference<LoadTestResult> lastResult = new AtomicReference<>(null);

    public LoadTestService(MatchingEngineManager manager, MeterRegistry meterRegistry) {
        this.manager = manager;

        // Register Gauge meters so Prometheus & Actuator monitor load test benchmark metrics dynamically
        Gauge.builder("loadtest.throughput.ops_per_sec", () -> {
            LoadTestResult r = lastResult.get();
            return r != null ? r.getThroughputOpsPerSec() : 0.0;
        }).description("Throughput in orders matched per second").register(meterRegistry);

        Gauge.builder("loadtest.duration.seconds", () -> {
            LoadTestResult r = lastResult.get();
            return r != null ? r.getDurationSeconds() : 0.0;
        }).description("Duration of last load test benchmark in seconds").register(meterRegistry);
    }

    public LoadTestResult executeBenchmark(int threads, int ordersPerThread) {
        log.info("Starting multithreaded load test benchmark with threads={} ordersPerThread={}", threads, ordersPerThread);

        ExecutorService executor = Executors.newFixedThreadPool(threads);
        long start = System.nanoTime();

        for (int t = 0; t < threads; t++) {
            executor.submit(() -> {
                for (int i = 0; i < ordersPerThread; i++) {
                    Order order = new Order(
                            "bench-" + Thread.currentThread().getId() + "-" + i,
                            "AAPL",
                            i % 2 == 0 ? OrderSide.BUY : OrderSide.SELL,
                            OrderType.LIMIT,
                            100.0,
                            10,
                            System.nanoTime()
                    );
                    manager.submitOrder(order);
                }
            });
        }

        executor.shutdown();
        try {
            executor.awaitTermination(5, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        long end = System.nanoTime();
        double durationSeconds = (end - start) / 1_000_000_000.0;
        int totalOrders = threads * ordersPerThread;
        double throughput = durationSeconds > 0 ? (totalOrders / durationSeconds) : 0;
        double avgLatencyUs = durationSeconds > 0 ? ((durationSeconds * 1_000_000.0) / totalOrders) : 0;

        LoadTestResult result = new LoadTestResult(
                totalOrders,
                threads,
                durationSeconds,
                throughput,
                avgLatencyUs,
                System.currentTimeMillis()
        );

        lastResult.set(result);
        log.info("Load test benchmark complete: Total Orders={} Duration={}s Throughput={} ops/sec",
                totalOrders, String.format("%.4f", durationSeconds), String.format("%.2f", throughput));

        return result;
    }

    public LoadTestResult getLastResult() {
        return lastResult.get();
    }
}
