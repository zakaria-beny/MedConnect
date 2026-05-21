package com.mediconnect.notification.service;

import com.mediconnect.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DeduplicationService {

    private final NotificationRepository notificationRepository;

    public boolean isDuplicate(
            String userId, String eventType, String resourceId) {
        return notificationRepository
                .existsByUserIdAndEventTypeAndResourceId(
                        userId, eventType, resourceId);
    }
}