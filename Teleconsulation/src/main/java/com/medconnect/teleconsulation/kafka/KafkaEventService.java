package com.medconnect.teleconsulation.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaEventService {

    private final KafkaTemplate<String, String> kafkaTemplate;

    public void publish(String topic, String sessionId) {
        try {
            kafkaTemplate.send(topic, sessionId, sessionId);
            log.info("Published event to topic '{}' for session '{}'", topic, sessionId);
        } catch (Exception ex) {
            log.warn("Failed to publish event to topic '{}': {}", topic, ex.getMessage());
        }
    }

    public void publish(String topic, String key, String payload) {
        try {
            kafkaTemplate.send(topic, key, payload);
            log.info("Published event to topic '{}' with key '{}'", topic, key);
        } catch (Exception ex) {
            log.warn("Failed to publish event to topic '{}': {}", topic, ex.getMessage());
        }
    }
}
