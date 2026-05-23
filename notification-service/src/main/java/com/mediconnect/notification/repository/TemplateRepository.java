package com.mediconnect.notification.repository;

import com.mediconnect.notification.document.NotificationTemplate;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;

public interface TemplateRepository
        extends MongoRepository<NotificationTemplate, String> {
    Optional<NotificationTemplate> findByEventTypeAndChannel(
            String eventType, String channel);
}