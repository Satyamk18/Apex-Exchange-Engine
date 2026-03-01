package com.apex.exchange.engine.kafka;


import com.apex.exchange.engine.model.Order;
import com.apex.exchange.engine.model.OrderEvent;
import com.apex.exchange.engine.service.MatchingEngine;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class OrderConsumer {

    private final MatchingEngine matchingEngine;

    public OrderConsumer(MatchingEngine matchingEngine) {
        this.matchingEngine = matchingEngine;
    }

    @KafkaListener(
            topics = "orders",
            groupId = "matching-engine-group"
    )
    public void consume(OrderEvent event) {

        System.out.println("Received order from Kafka: " + event.getOrderId());

        Order order = new Order(
                event.getOrderId(),
                event.getSymbol(),
                event.getSide(),
                event.getPrice(),
                event.getQuantity(),
                event.getTimestamp()
        );

        matchingEngine.match(order);
    }
}