package com.apex.exchange.engine.kafka;

import com.apex.exchange.engine.model.TradeEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class TradeProducer {

    private final KafkaTemplate<String, TradeEvent> kafkaTemplate;

    public TradeProducer(KafkaTemplate<String, TradeEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishTrade(TradeEvent event) {
        kafkaTemplate.send("trades", event.getSymbol(), event);
    }
}