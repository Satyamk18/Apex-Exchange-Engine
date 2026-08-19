package com.apex.exchange.engine.service;

import com.apex.exchange.engine.metrics.EngineLatencyMetrics;
import com.apex.exchange.engine.model.*;
import com.apex.exchange.engine.snapshot.SnapshotService;
import com.apex.exchange.engine.websocket.MarketDataBroadcaster;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.YieldingWaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import com.lmax.disruptor.util.DaemonThreadFactory;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class SymbolEngine {

    private static final int SNAPSHOT_INTERVAL = 1000;
    private static final int RING_BUFFER_SIZE = 1024 * 16; // 16384 (Power of 2 for zero-lock bitwise masking)

    private final String symbol;
    private final MatchingEngine matchingEngine;
    private final TradePublisher tradePublisher;
    private final OrderStatusTracker orderStatusTracker;
    private final SnapshotService snapshotService;
    private final MarketDataBroadcaster marketDataBroadcaster;
    private final EngineLatencyMetrics engineLatencyMetrics;

    private final Disruptor<OrderEventHolder> disruptor;
    private final RingBuffer<OrderEventHolder> ringBuffer;
    private final AtomicLong processedCount = new AtomicLong(0);

    public SymbolEngine(String symbol,
                        MatchingEngine matchingEngine,
                        TradePublisher tradePublisher,
                        OrderStatusTracker orderStatusTracker,
                        SnapshotService snapshotService,
                        MarketDataBroadcaster marketDataBroadcaster) {
        this(symbol, matchingEngine, tradePublisher, orderStatusTracker, snapshotService, marketDataBroadcaster, null);
    }

    public SymbolEngine(String symbol,
                        MatchingEngine matchingEngine,
                        TradePublisher tradePublisher,
                        OrderStatusTracker orderStatusTracker,
                        SnapshotService snapshotService,
                        MarketDataBroadcaster marketDataBroadcaster,
                        EngineLatencyMetrics engineLatencyMetrics) {
        this.symbol = symbol;
        this.matchingEngine = matchingEngine;
        this.tradePublisher = tradePublisher;
        this.orderStatusTracker = orderStatusTracker;
        this.snapshotService = snapshotService;
        this.marketDataBroadcaster = marketDataBroadcaster;
        this.engineLatencyMetrics = engineLatencyMetrics;

        // Initialize LMAX Disruptor for high-throughput zero-lock event ring buffer
        this.disruptor = new Disruptor<>(
                OrderEventHolder::new,
                RING_BUFFER_SIZE,
                DaemonThreadFactory.INSTANCE,
                ProducerType.MULTI,
                new YieldingWaitStrategy()
        );

        this.disruptor.handleEventsWith(this::handleOrderEvent);
        this.disruptor.start();
        this.ringBuffer = this.disruptor.getRingBuffer();
    }

    public void submit(OrderEvent event) {
        ringBuffer.publishEvent((holder, sequence, arg) -> holder.set(arg), event);
    }

    public MatchingEngine getMatchingEngine() {
        return matchingEngine;
    }

    private void handleOrderEvent(OrderEventHolder holder, long sequence, boolean endOfBatch) {
        OrderEvent event = holder.get();
        if (event == null) return;

        long startNanos = System.nanoTime();
        try {
            if (event.getAction() == OrderAction.CANCEL) {
                matchingEngine.cancelOrder(event.getOrderId(), orderStatusTracker);
            } else {
                Order order = new Order(
                        event.getOrderId(),
                        event.getSymbol(),
                        event.getSide(),
                        event.getType(),
                        event.getPrice(),
                        event.getQuantity(),
                        event.getTimestamp()
                );
                List<Trade> trades = matchingEngine.process(order, orderStatusTracker);
                publishTrades(trades);
            }

            // Broadcast real-time L2 order book depth update via WebSocket
            if (marketDataBroadcaster != null) {
                OrderBookDepthDto depthDto = matchingEngine.getOrderBook().getDepth(symbol, 10);
                marketDataBroadcaster.broadcastDepth(depthDto);
            }

            // Periodic snapshot after every SNAPSHOT_INTERVAL operations
            long count = processedCount.incrementAndGet();
            if (count % SNAPSHOT_INTERVAL == 0 && snapshotService != null) {
                snapshotService.saveSnapshot(symbol, matchingEngine, count);
            }

            // Record latency percentiles if Micrometer metrics component is present
            if (engineLatencyMetrics != null) {
                engineLatencyMetrics.recordSymbolProcessing(System.nanoTime() - startNanos);
                if (event.getTimestamp() > 0) {
                    engineLatencyMetrics.recordOrderLatency(event.getTimestamp());
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            holder.clear(); // Zero-allocation cleanup
        }
    }

    private void publishTrades(List<Trade> trades) {
        if (tradePublisher != null) {
            for (Trade trade : trades) {
                tradePublisher.publish(trade);
            }
        }
    }

    public void shutdown() {
        disruptor.shutdown();
    }

    /**
     * Mutable reusable event container for LMAX Disruptor ring buffer slots.
     */
    public static class OrderEventHolder {
        private OrderEvent event;

        public OrderEvent get() {
            return event;
        }

        public void set(OrderEvent event) {
            this.event = event;
        }

        public void clear() {
            this.event = null;
        }
    }
}
