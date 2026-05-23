package com.mediconnect.notification.repository;

import com.mediconnect.notification.document.DeliveryLog;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface DeliveryLogRepository
        extends MongoRepository<DeliveryLog, String> {
    List<DeliveryLog> findByNotificationId(String notificationId);
    List<DeliveryLog> findByStatus(String status);
}