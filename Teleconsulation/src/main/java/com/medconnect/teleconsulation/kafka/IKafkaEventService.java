package com.medconnect.teleconsulation.kafka;

public interface IKafkaEventService {
    void publish(String topic, String sessionId);
    void publish(String topic, String key, String payload);
}
