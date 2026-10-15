package com.apex.exchange.engine.snapshot;

import com.apex.exchange.engine.model.Order;
import com.apex.exchange.engine.service.MatchingEngine;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Service
public class SnapshotService {

    private static final Logger log = LoggerFactory.getLogger(SnapshotService.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final String snapshotDirectory;
    private final ExecutorService snapshotExecutor;

    public SnapshotService(@Value("${exchange.snapshot.directory:.snapshots}") String snapshotDirectory) {
        this.snapshotDirectory = snapshotDirectory;
        this.snapshotExecutor = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "snapshot-io-worker");
            t.setDaemon(true);
            return t;
        });
        ensureDirectoryExists();
    }

    /**
     * Captures an in-memory structural snapshot of the order book state in microseconds.
     */
    public OrderBookSnapshot createSnapshot(String symbol, MatchingEngine engine, long sequence) {
        List<Order> buyOrders = new ArrayList<>(engine.getOrderBook().getBuyOrders());
        List<Order> sellOrders = new ArrayList<>(engine.getOrderBook().getSellOrders());

        return new OrderBookSnapshot(
                symbol,
                System.currentTimeMillis(),
                sequence,
                buyOrders,
                sellOrders
        );
    }

    /**
     * Synchronously persists a snapshot to disk using atomic temporary file moves.
     */
    public void saveSnapshot(String symbol, MatchingEngine engine, long sequence) {
        OrderBookSnapshot snapshot = createSnapshot(symbol, engine, sequence);
        persistSnapshotToFile(snapshot);
    }

    /**
     * Asynchronously offloads JSON serialization and disk writing to a background worker,
     * completely eliminating disk I/O pauses from the matching engine thread.
     */
    public CompletableFuture<Void> saveSnapshotAsync(OrderBookSnapshot snapshot) {
        return CompletableFuture.runAsync(() -> persistSnapshotToFile(snapshot), snapshotExecutor);
    }

    private void persistSnapshotToFile(OrderBookSnapshot snapshot) {
        File file = snapshotFile(snapshot.getSymbol());
        File tmpFile = new File(snapshotDirectory + File.separator + snapshot.getSymbol() + ".snapshot.json.tmp");
        try {
            OBJECT_MAPPER.writeValue(tmpFile, snapshot);
            try {
                Files.move(tmpFile.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(tmpFile.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
            log.info("Snapshot saved for symbol={} sequence={}", snapshot.getSymbol(), snapshot.getSnapshotSequence());
        } catch (IOException e) {
            log.error("Failed to save snapshot for symbol={}: {}", snapshot.getSymbol(), e.getMessage());
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

    @PreDestroy
    public void shutdown() {
        snapshotExecutor.shutdown();
        try {
            if (!snapshotExecutor.awaitTermination(2, TimeUnit.SECONDS)) {
                snapshotExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            snapshotExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
