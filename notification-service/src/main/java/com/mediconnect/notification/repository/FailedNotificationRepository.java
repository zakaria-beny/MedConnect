package com.mediconnect.notification.repository;

import com.mediconnect.notification.document.FailedNotification;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface FailedNotificationRepository
        extends MongoRepository<FailedNotification, String> {
    List<FailedNotification> findByUserId(String userId);
    List<FailedNotification> findByDeadLetterTrue();
}