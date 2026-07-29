package com.apex.exchange.engine.kafka;

import com.apex.exchange.engine.model.Trade;
import com.apex.exchange.engine.model.TradeEvent;
import com.apex.exchange.engine.service.TradePublisher;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class TradeProducer implements TradePublisher {

    private final KafkaTemplate<String, TradeEvent> kafkaTemplate;
    private final KafkaTopicProperties topicProperties;

    public TradeProducer(KafkaTemplate<String, TradeEvent> kafkaTemplate,
                         KafkaTopicProperties topicProperties) {
        this.kafkaTemplate = kafkaTemplate;
        this.topicProperties = topicProperties;
    }

    @Override
    public void publish(Trade trade) {
        publishTrade(new TradeEvent(
                trade.getTradeId(),
                trade.getSymbol(),
                trade.getBuyOrderId(),
                trade.getSellOrderId(),
                trade.getPrice(),
                trade.getQuantity(),
                trade.getTimestamp()
        ));
    }

    public void publishTrade(TradeEvent event) {
        kafkaTemplate.send(topicProperties.getTrades().getName(), event.getSymbol(), event);
    }
}
