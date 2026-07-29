package com.apex.exchange.engine.kafka;

import com.apex.exchange.engine.model.Trade;
import com.apex.exchange.engine.model.TradeEvent;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.kafka.core.KafkaTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.ArgumentMatchers.eq;

class TradeProducerTest {

    @Test
    void publishesTradeEventToTradesTopic() {
        KafkaTemplate<String, TradeEvent> kafkaTemplate = mock(KafkaTemplate.class);
        KafkaTopicProperties topicProperties = new KafkaTopicProperties();
        topicProperties.getTrades().setName("trades");
        TradeProducer producer = new TradeProducer(kafkaTemplate, topicProperties);
        Trade trade = new Trade("trade-1", "AAPL", 101.50, 25, "buy-1", "sell-1", 123456789L);

        producer.publish(trade);

        ArgumentCaptor<TradeEvent> eventCaptor = ArgumentCaptor.forClass(TradeEvent.class);
        verify(kafkaTemplate).send(eq("trades"), eq("AAPL"), eventCaptor.capture());

        TradeEvent event = eventCaptor.getValue();
        assertEquals("trade-1", event.getTradeId());
        assertEquals("AAPL", event.getSymbol());
        assertEquals("buy-1", event.getBuyOrderId());
        assertEquals("sell-1", event.getSellOrderId());
        assertEquals(101.50, event.getPrice());
        assertEquals(25, event.getQuantity());
        assertEquals(123456789L, event.getTimestamp());
    }
}
