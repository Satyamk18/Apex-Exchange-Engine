package com.apex.exchange.engine.kafka;

import com.apex.exchange.engine.model.OrderEvent;
import com.apex.exchange.engine.service.MatchingEngineManager;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class OrderConsumer {

    private final MatchingEngineManager manager;

    public OrderConsumer(MatchingEngineManager manager) {
        this.manager = manager;
    }

    @KafkaListener(topics = "${exchange.kafka.orders.name}")
    public void consume(OrderEvent event) {
        manager.processEvent(event);
    }
}
