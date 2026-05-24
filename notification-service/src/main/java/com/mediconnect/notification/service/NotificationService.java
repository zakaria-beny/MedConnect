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
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final EmailService emailService;
    private final InAppNotificationService inAppNotificationService;

    public void sendNotification(NotificationRequest request) {
        for (String channel : request.getChannels()) {
            switch (channel.toUpperCase()) {
                case "EMAIL" -> emailService.sendEmail(
                        request.getUserId(),
                        request.getTitle(),
                        request.getContent()
                );
                case "IN_APP" -> inAppNotificationService
                        .storeNotification(request);
            }
        }
    }

    public Notification publishNotification(
            String userId, String type, String content) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setType(type);
        notification.setContent(content);
        notification.setStatus("PENDING");
        notification.setRead(false);
        notification.setCreatedAt(LocalDateTime.now());
        return notificationRepository.save(notification);
    }
}