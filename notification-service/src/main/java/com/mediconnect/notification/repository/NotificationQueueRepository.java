package com.mediconnect.notification.repository;

import com.mediconnect.notification.document.NotificationQueue;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface NotificationQueueRepository
        extends MongoRepository<NotificationQueue, String> {
    List<NotificationQueue> findByStatus(String status);
    List<NotificationQueue> findByUserIdAndStatus(String userId, String status);
    List<NotificationQueue> findByUrgentTrue();
}