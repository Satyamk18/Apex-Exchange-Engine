package com.apex.exchange.engine.kafka;

import com.apex.exchange.engine.model.OrderEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class OrderProducer {

    private final KafkaTemplate<String, OrderEvent> kafkaTemplate;
    private final KafkaTopicProperties topicProperties;

    public OrderProducer(KafkaTemplate<String, OrderEvent> kafkaTemplate,
                         KafkaTopicProperties topicProperties) {
        this.kafkaTemplate = kafkaTemplate;
        this.topicProperties = topicProperties;
    }

    public void publishOrder(OrderEvent event) {
        kafkaTemplate.send(topicProperties.getOrders().getName(), event.getSymbol(), event);
    }
}
