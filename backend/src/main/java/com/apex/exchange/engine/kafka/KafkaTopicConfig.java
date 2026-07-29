package com.apex.exchange.engine.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
@EnableConfigurationProperties(KafkaTopicProperties.class)
public class KafkaTopicConfig {

    @Bean
    public NewTopic ordersTopic(KafkaTopicProperties properties) {
        return buildTopic(properties.getOrders());
    }

    @Bean
    public NewTopic tradesTopic(KafkaTopicProperties properties) {
        return buildTopic(properties.getTrades());
    }

    private NewTopic buildTopic(KafkaTopicProperties.Topic topic) {
        return TopicBuilder.name(topic.getName())
                .partitions(topic.getPartitions())
                .replicas(topic.getReplicationFactor())
                .build();
    }
}
