package com.medconnect.userservice.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class UserManagementEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(UserManagementEventPublisher.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${medconnect.kafka.topics.user-created:user.created}")
    private String userCreatedTopic;

    @Value("${medconnect.kafka.topics.user-updated:user.updated}")
    private String userUpdatedTopic;

    @Value("${medconnect.kafka.topics.user-suspended:user.suspended}")
    private String userSuspendedTopic;

    @Value("${medconnect.kafka.topics.user-deleted:user.deleted}")
    private String userDeletedTopic;

    @Value("${medconnect.kafka.topics.subscription-upgraded:subscription.upgraded}")
    private String subscriptionUpgradedTopic;

    @Value("${medconnect.kafka.topics.subscription-downgraded:subscription.downgraded}")
    private String subscriptionDowngradedTopic;

    public UserManagementEventPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishUserCreated(String userId, String email, String source) {
        publish(userCreatedTopic, "user.created", userId, email, Map.of("source", source));
    }

    public void publishUserUpdated(String userId, String email) {
        publish(userUpdatedTopic, "user.updated", userId, email, Map.of());
    }

    public void publishUserSuspended(String userId, String email) {
        publish(userSuspendedTopic, "user.suspended", userId, email, Map.of());
    }

    public void publishUserDeleted(String userId, String email) {
        publish(userDeletedTopic, "user.deleted", userId, email, Map.of());
    }

    public void publishSubscriptionUpgraded(String userId, String fromPlan, String toPlan) {
        publish(subscriptionUpgradedTopic, "subscription.upgraded", userId, null, Map.of(
                "fromPlan", fromPlan,
                "toPlan", toPlan
        ));
    }

    public void publishSubscriptionDowngraded(String userId, String fromPlan, String toPlan) {
        publish(subscriptionDowngradedTopic, "subscription.downgraded", userId, null, Map.of(
                "fromPlan", fromPlan,
                "toPlan", toPlan
        ));
    }

    private void publish(String topic, String eventType, String userId, String email, Map<String, Object> details) {
        Map<String, Object> event = new LinkedHashMap<>();
        event.put("eventType", eventType);
        event.put("timestamp", Instant.now().toString());
        event.put("userId", userId);
        event.put("email", email);
        event.put("details", details);
        String key = userId != null ? userId : email;
        try {
            kafkaTemplate.send(topic, key, event);
        } catch (Exception e) {
            log.warn("Failed to publish Kafka event [{}] for {}: {}", eventType, key, e.getMessage());
        }
    }
}
