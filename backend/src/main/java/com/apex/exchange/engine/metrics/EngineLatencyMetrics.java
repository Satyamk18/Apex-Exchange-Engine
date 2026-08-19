package com.apex.exchange.engine.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class EngineLatencyMetrics {

    private final Timer orderMatchingTimer;
    private final Timer symbolProcessingTimer;

    public EngineLatencyMetrics(MeterRegistry registry) {
        this.orderMatchingTimer = Timer.builder("matching.engine.order.latency")
                .description("End-to-end order ingestion to trade execution latency")
                .publishPercentiles(0.5, 0.95, 0.99)
                .register(registry);

        this.symbolProcessingTimer = Timer.builder("matching.engine.symbol.processing.time")
                .description("Pure symbol engine matching execution time")
                .publishPercentiles(0.5, 0.95, 0.99)
                .register(registry);
    }

    public void recordOrderLatency(long startNanoTime) {
        long elapsedNanos = System.nanoTime() - startNanoTime;
        orderMatchingTimer.record(elapsedNanos, TimeUnit.NANOSECONDS);
    }

    public void recordSymbolProcessing(long durationNanos) {
        symbolProcessingTimer.record(durationNanos, TimeUnit.NANOSECONDS);
    }
}
