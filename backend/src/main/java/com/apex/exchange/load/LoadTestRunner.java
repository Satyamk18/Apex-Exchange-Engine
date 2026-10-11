package com.apex.exchange.load;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("load-test")
public class LoadTestRunner implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(LoadTestRunner.class);

    private final LoadTestService loadTestService;

    public LoadTestRunner(LoadTestService loadTestService) {
        this.loadTestService = loadTestService;
    }

    @Override
    public void run(String... args) throws Exception {
        LoadTestResult result = loadTestService.executeBenchmark(8, 10000);

        log.info("=================================");
        log.info("Total Orders: {}", result.getTotalOrders());
        log.info("Time Taken (seconds): {}", result.getDurationSeconds());
        log.info("Throughput (orders/sec): {}", result.getThroughputOpsPerSec());
        log.info("Average Latency per Order (µs): {}", result.getAvgLatencyUs());
        log.info("=================================");
    }
}
