package com.medconnect.userservice.security.events;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class AuthEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${medconnect.kafka.topics.user-login:user.login}")
    private String userLoginTopic;

    @Value("${medconnect.kafka.topics.user-logout:user.logout}")
    private String userLogoutTopic;

    @Value("${medconnect.kafka.topics.auth-failed:auth.failed}")
    private String authFailedTopic;

    @Value("${medconnect.kafka.topics.mfa-required:mfa.required}")
    private String mfaRequiredTopic;

    @Value("${medconnect.kafka.topics.mfa-verified:mfa.verified}")
    private String mfaVerifiedTopic;

    public AuthEventPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishUserLogin(String userId, String email, String sessionId, String method) {
        publish(userLoginTopic, "user.login", userId, email, Map.of(
                "sessionId", sessionId,
                "method", method
        ));
    }

    public void publishUserLogout(String userId, String email, String sessionId) {
        publish(userLogoutTopic, "user.logout", userId, email, Map.of(
                "sessionId", sessionId
        ));
    }

    public void publishAuthFailed(String email, String reason) {
        publish(authFailedTopic, "auth.failed", null, email, Map.of(
                "reason", reason
        ));
    }

    public void publishMfaRequired(String userId, String email, String method) {
        publish(mfaRequiredTopic, "mfa.required", userId, email, Map.of(
                "method", method
        ));
    }

    public void publishMfaVerified(String userId, String email, String method) {
        publish(mfaVerifiedTopic, "mfa.verified", userId, email, Map.of(
                "method", method
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
        kafkaTemplate.send(topic, key, event);
    }
}
