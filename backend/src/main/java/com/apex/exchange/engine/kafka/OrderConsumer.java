package com.apex.exchange.engine.kafka;

import com.apex.exchange.engine.model.Order;
import com.apex.exchange.engine.model.OrderEvent;
import com.apex.exchange.engine.service.MatchingEngineManager;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class OrderConsumer {

    private final MatchingEngineManager engineManager;

    public OrderConsumer(MatchingEngineManager engineManager) {
        this.engineManager = engineManager;
    }

    @KafkaListener(topics = "orders")
    public void consume(OrderEvent event) {

        Order order = new Order(
                event.getOrderId(),
                event.getSymbol(),
                event.getSide(),
                event.getPrice(),
                event.getQuantity(),
                event.getTimestamp()
        );

        engineManager.submitOrder(order);
    }
}