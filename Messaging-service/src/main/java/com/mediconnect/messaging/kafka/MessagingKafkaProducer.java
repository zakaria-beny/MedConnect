package com.mediconnect.messaging.kafka;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class MessagingKafkaProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishMessageSent(String messageId, String senderId, String conversationId) {
        Map<String, String> event = new HashMap<>();
        event.put("messageId", messageId);
        event.put("senderId", senderId);
        event.put("conversationId", conversationId);
        event.put("event", "message.sent");
        kafkaTemplate.send("message.sent", event);
    }

    public void publishMessageRead(String messageId, String userId) {
        Map<String, String> event = new HashMap<>();
        event.put("messageId", messageId);
        event.put("userId", userId);
        event.put("event", "message.read");
        kafkaTemplate.send("message.read", event);
    }

    public void publishMessageDelivered(String messageId, String userId) {
        Map<String, String> event = new HashMap<>();
        event.put("messageId", messageId);
        event.put("userId", userId);
        event.put("event", "message.delivered");
        kafkaTemplate.send("message.delivered", event);
    }
}