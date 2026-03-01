package com.apex.exchange.engine.service;

import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Component;

@Component
public class EngineShutdownManager {

    private final MatchingEngineManager manager;

    public EngineShutdownManager(MatchingEngineManager manager) {
        this.manager = manager;
    }

    @PreDestroy
    public void onShutdown() {

        System.out.println("Shutting down... Snapshotting order books");

        manager.getAllEngines().forEach((symbol, engine) -> {
            System.out.println("Symbol: " + symbol);
            System.out.println(engine.snapshot());
        });

        System.out.println("Snapshot complete.");
    }
}