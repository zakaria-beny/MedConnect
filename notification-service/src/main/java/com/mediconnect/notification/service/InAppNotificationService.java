package com.mediconnect.notification.service;

import com.mediconnect.notification.document.Notification;
import com.mediconnect.notification.dto.NotificationRequest;
import com.mediconnect.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InAppNotificationService {

    private final NotificationRepository notificationRepository;

    public Notification storeNotification(NotificationRequest request) {
        Notification notification = new Notification();
        notification.setUserId(request.getUserId());
        notification.setType(request.getType());
        notification.setTitle(request.getTitle());
        notification.setContent(request.getContent());
        notification.setChannel("IN_APP");
        notification.setStatus("DELIVERED");
        notification.setRead(false);
        notification.setCreatedAt(LocalDateTime.now());
        notification.setSentAt(LocalDateTime.now());
        notification.setResourceId(request.getResourceId());
        notification.setEventType(request.getEventType());
        return notificationRepository.save(notification);
    }

    public List<Notification> getNotifications(String userId) {
        return notificationRepository.findByUserId(userId);
    }

    public List<Notification> getUnreadNotifications(String userId) {
        return notificationRepository.findByUserIdAndReadFalse(userId);
    }

    public Notification markAsRead(String notificationId) {
        Notification notification = notificationRepository
                .findById(notificationId)
                .orElseThrow(() ->
                        new RuntimeException("Notification non trouvée"));
        notification.setRead(true);
        notification.setReadAt(LocalDateTime.now());
        return notificationRepository.save(notification);
    }

    public void deleteNotification(String notificationId) {
        notificationRepository.deleteById(notificationId);
    }
}