package com.apex.exchange.engine.kafka;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "exchange.kafka")
public class KafkaTopicProperties {

    private final Topic orders = new Topic();
    private final Topic trades = new Topic();

    public Topic getOrders() {
        return orders;
    }

    public Topic getTrades() {
        return trades;
    }

    public static class Topic {

        private String name;
        private int partitions;
        private short replicationFactor;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getPartitions() {
            return partitions;
        }

        public void setPartitions(int partitions) {
            this.partitions = partitions;
        }

        public short getReplicationFactor() {
            return replicationFactor;
        }

        public void setReplicationFactor(short replicationFactor) {
            this.replicationFactor = replicationFactor;
        }
    }
}
