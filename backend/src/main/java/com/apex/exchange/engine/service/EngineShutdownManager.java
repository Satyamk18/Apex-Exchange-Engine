package com.apex.exchange.engine.service;

import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class EngineShutdownManager {

    private static final Logger log = LoggerFactory.getLogger(EngineShutdownManager.class);

    private final MatchingEngineManager manager;

    public EngineShutdownManager(MatchingEngineManager manager) {
        this.manager = manager;
    }

    @PreDestroy
    public void onShutdown() {
        log.info("Gracefully shutting down matching engines...");
        manager.getAllEngines().forEach(SymbolEngine::shutdown);
        log.info("All matching engines shut down successfully.");
    }
}