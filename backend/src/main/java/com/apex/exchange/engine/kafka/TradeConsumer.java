package com.apex.exchange.engine.kafka;

import com.apex.exchange.engine.model.TradeEvent;
import com.apex.exchange.engine.model.Trade;
import com.apex.exchange.engine.persistence.service.PersistenceService;
import com.apex.exchange.engine.websocket.MarketDataBroadcaster;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class TradeConsumer {

    private static final Logger log = LoggerFactory.getLogger(TradeConsumer.class);

    private final PersistenceService persistenceService;
    private final MarketDataBroadcaster marketDataBroadcaster;

    public TradeConsumer(PersistenceService persistenceService, MarketDataBroadcaster marketDataBroadcaster) {
        this.persistenceService = persistenceService;
        this.marketDataBroadcaster = marketDataBroadcaster;
    }

    @KafkaListener(topics = "${exchange.kafka.trades.name}", groupId = "trade-listener-group")
    public void consume(TradeEvent event) {
        log.info("Trade received: id={} symbol={} qty={} price={}",
                event.getTradeId(), event.getSymbol(), event.getQuantity(), event.getPrice());

        Trade trade = new Trade(
                event.getTradeId(),
                event.getSymbol(),
                event.getPrice(),
                event.getQuantity(),
                event.getBuyOrderId(),
                event.getSellOrderId(),
                event.getTimestamp()
        );

        // 1. Asynchronously persist to DB
        persistenceService.saveTrade(trade);

        // 2. Broadcast live trade event & ticker to WebSocket subscribers
        marketDataBroadcaster.broadcastTrade(event);
    }
}
