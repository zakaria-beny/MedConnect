package com.mediconnect.notification.repository;

import com.mediconnect.notification.document.Notification;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface NotificationRepository extends MongoRepository<Notification, String> {
    List<Notification> findByUserId(String userId);
    List<Notification> findByUserIdAndReadFalse(String userId);
    boolean existsByUserIdAndEventTypeAndResourceId(
            String userId, String eventType, String resourceId);
}