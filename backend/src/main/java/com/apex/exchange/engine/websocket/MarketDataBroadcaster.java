package com.apex.exchange.engine.websocket;

import com.apex.exchange.engine.model.OrderBookDepthDto;
import com.apex.exchange.engine.model.TickerDto;
import com.apex.exchange.engine.model.TradeEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class MarketDataBroadcaster {

    private static final Logger log = LoggerFactory.getLogger(MarketDataBroadcaster.class);

    private final SimpMessagingTemplate messagingTemplate;
    private final Map<String, TickerStats> tickerMap = new ConcurrentHashMap<>();

    public MarketDataBroadcaster(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void broadcastTrade(TradeEvent trade) {
        String topic = "/topic/trades/" + trade.getSymbol();
        log.debug("Broadcasting trade to {}", topic);
        messagingTemplate.convertAndSend(topic, trade);

        // Update rolling ticker stats and broadcast ticker update
        TickerStats stats = tickerMap.computeIfAbsent(trade.getSymbol(), s -> new TickerStats(trade.getSymbol()));
        stats.update(trade.getPrice(), trade.getQuantity());

        TickerDto ticker = stats.toDto();
        String tickerTopic = "/topic/ticker/" + trade.getSymbol();
        messagingTemplate.convertAndSend(tickerTopic, ticker);
    }

    public void broadcastDepth(OrderBookDepthDto depth) {
        String topic = "/topic/depth/" + depth.getSymbol();
        log.debug("Broadcasting L2 order book depth to {}", topic);
        messagingTemplate.convertAndSend(topic, depth);
    }

    private static class TickerStats {
        private final String symbol;
        private double lastPrice;
        private double highPrice = Double.MIN_VALUE;
        private double lowPrice = Double.MAX_VALUE;
        private long totalVolume = 0;

        public TickerStats(String symbol) {
            this.symbol = symbol;
        }

        public synchronized void update(double price, long quantity) {
            this.lastPrice = price;
            this.highPrice = Math.max(this.highPrice, price);
            this.lowPrice = Math.min(this.lowPrice, price);
            this.totalVolume += quantity;
        }

        public synchronized TickerDto toDto() {
            return new TickerDto(
                    symbol,
                    lastPrice,
                    highPrice == Double.MIN_VALUE ? lastPrice : highPrice,
                    lowPrice == Double.MAX_VALUE ? lastPrice : lowPrice,
                    totalVolume,
                    System.currentTimeMillis()
            );
        }
    }
}
