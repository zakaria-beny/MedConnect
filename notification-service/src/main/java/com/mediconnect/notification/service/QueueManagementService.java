package com.mediconnect.notification.service;

import com.mediconnect.notification.document.FailedNotification;
import com.mediconnect.notification.document.NotificationQueue;
import com.mediconnect.notification.dto.NotificationRequest;
import com.mediconnect.notification.repository.FailedNotificationRepository;
import com.mediconnect.notification.repository.NotificationQueueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class QueueManagementService {

    private final NotificationQueueRepository queueRepository;
    private final FailedNotificationRepository failedNotificationRepository;
    private final NotificationService notificationService;

    public NotificationQueue enqueueNotification(NotificationRequest request) {
        NotificationQueue queue = new NotificationQueue();
        queue.setUserId(request.getUserId());
        queue.setType(request.getType());
        queue.setTitle(request.getTitle());
        queue.setContent(request.getContent());
        queue.setChannels(request.getChannels());
        queue.setStatus("PENDING");
        queue.setAttempts(0);
        queue.setMaxAttempts(3);
        queue.setCreatedAt(LocalDateTime.now());
        queue.setEventType(request.getEventType());
        queue.setResourceId(request.getResourceId());
        return queueRepository.save(queue);
    }

    public void processQueue() {
        List<NotificationQueue> pending =
                queueRepository.findByStatus("PENDING");

        // Traiter les urgents en premier
        List<NotificationQueue> urgent =
                queueRepository.findByUrgentTrue();

        for (NotificationQueue item : urgent) {
            processItem(item);
        }

        for (NotificationQueue item : pending) {
            processItem(item);
        }
    }

    private void processItem(NotificationQueue item) {
        try {
            item.setStatus("PROCESSING");
            item.setAttempts(item.getAttempts() + 1);

            NotificationRequest request = new NotificationRequest();
            request.setUserId(item.getUserId());
            request.setType(item.getType());
            request.setTitle(item.getTitle());
            request.setContent(item.getContent());
            request.setChannels(item.getChannels());

            notificationService.sendNotification(request);

            item.setStatus("SENT");
            item.setProcessedAt(LocalDateTime.now());
            queueRepository.save(item);

        } catch (Exception e) {
            retryFailed(item.getId());
        }
    }

    public void retryFailed(String notificationId) {
        queueRepository.findById(notificationId).ifPresent(item -> {
            if (item.getAttempts() >= item.getMaxAttempts()) {
                deadLetterQueue(notificationId);
            } else {
                // Exponential backoff
                item.setStatus("PENDING");
                item.setScheduledAt(
                        LocalDateTime.now().plusMinutes(
                                (long) Math.pow(2, item.getAttempts())));
                queueRepository.save(item);
            }
        });
    }

    public void deadLetterQueue(String notificationId) {
        queueRepository.findById(notificationId).ifPresent(item -> {
            item.setStatus("DEAD_LETTER");
            queueRepository.save(item);

            FailedNotification failed = new FailedNotification();
            failed.setNotificationId(notificationId);
            failed.setUserId(item.getUserId());
            failed.setAttempts(item.getAttempts());
            failed.setFailedAt(LocalDateTime.now());
            failed.setDeadLetter(true);
            failedNotificationRepository.save(failed);
        });
    }
}