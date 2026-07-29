package com.apex.exchange.engine.kafka;

import com.apex.exchange.engine.model.TradeEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class TradeConsumer {

    @KafkaListener(topics = "trades", groupId = "trade-listener-group")
    public void consume(TradeEvent event) {
        System.out.println("Trade Event Received -> "
                + event.getTradeId()
                + " | "
                + event.getSymbol()
                + " | Qty: " + event.getQuantity()
                + " | Price: " + event.getPrice());
    }
}
