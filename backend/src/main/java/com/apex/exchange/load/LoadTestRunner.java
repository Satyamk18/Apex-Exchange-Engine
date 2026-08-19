package com.apex.exchange.load;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("load-test")
public class LoadTestRunner implements CommandLineRunner {

    private final LoadTestService loadTestService;

    public LoadTestRunner(LoadTestService loadTestService) {
        this.loadTestService = loadTestService;
    }

    @Override
    public void run(String... args) throws Exception {
        LoadTestResult result = loadTestService.executeBenchmark(8, 10000);

        System.out.println("=================================");
        System.out.println("Total Orders: " + result.getTotalOrders());
        System.out.println("Time Taken (seconds): " + result.getDurationSeconds());
        System.out.println("Throughput (orders/sec): " + result.getThroughputOpsPerSec());
        System.out.println("Average Latency per Order (µs): " + result.getAvgLatencyUs());
        System.out.println("=================================");
    }
}
