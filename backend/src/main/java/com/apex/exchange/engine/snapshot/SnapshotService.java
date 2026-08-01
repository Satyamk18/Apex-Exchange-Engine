package com.apex.exchange.engine.snapshot;

import com.apex.exchange.engine.model.Order;
import com.apex.exchange.engine.service.MatchingEngine;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class SnapshotService {

    private static final Logger log = LoggerFactory.getLogger(SnapshotService.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final String snapshotDirectory;

    public SnapshotService(@Value("${exchange.snapshot.directory:.snapshots}") String snapshotDirectory) {
        this.snapshotDirectory = snapshotDirectory;
        ensureDirectoryExists();
    }

    public void saveSnapshot(String symbol, MatchingEngine engine, long sequence) {
        List<Order> buyOrders = new ArrayList<>(engine.getOrderBook().getBuyOrders());
        List<Order> sellOrders = new ArrayList<>(engine.getOrderBook().getSellOrders());

        OrderBookSnapshot snapshot = new OrderBookSnapshot(
                symbol,
                System.currentTimeMillis(),
                sequence,
                buyOrders,
                sellOrders
        );

        File file = snapshotFile(symbol);
        try {
            OBJECT_MAPPER.writeValue(file, snapshot);
            log.info("Snapshot saved for symbol={} sequence={}", symbol, sequence);
        } catch (IOException e) {
            log.error("Failed to save snapshot for symbol={}: {}", symbol, e.getMessage());
        }
    }

    public OrderBookSnapshot loadSnapshot(String symbol) {
        File file = snapshotFile(symbol);
        if (!file.exists()) {
            return null;
        }
        try {
            OrderBookSnapshot snapshot = OBJECT_MAPPER.readValue(file, OrderBookSnapshot.class);
            log.info("Snapshot loaded for symbol={} sequence={}", symbol, snapshot.getSnapshotSequence());
            return snapshot;
        } catch (IOException e) {
            log.error("Failed to load snapshot for symbol={}: {}", symbol, e.getMessage());
            return null;
        }
    }

    public boolean hasSnapshot(String symbol) {
        return snapshotFile(symbol).exists();
    }

    public List<String> availableSymbols() {
        File dir = new File(snapshotDirectory);
        if (!dir.exists() || !dir.isDirectory()) {
            return Collections.emptyList();
        }
        File[] files = dir.listFiles((d, name) -> name.endsWith(".snapshot.json"));
        if (files == null) return Collections.emptyList();
        List<String> symbols = new ArrayList<>();
        for (File f : files) {
            String name = f.getName();
            symbols.add(name.substring(0, name.indexOf(".snapshot.json")));
        }
        return symbols;
    }

    private File snapshotFile(String symbol) {
        return new File(snapshotDirectory + File.separator + symbol + ".snapshot.json");
    }

    private void ensureDirectoryExists() {
        File dir = new File(snapshotDirectory);
        if (!dir.exists() && !dir.mkdirs()) {
            log.warn("Could not create snapshot directory: {}", snapshotDirectory);
        }
    }
}
