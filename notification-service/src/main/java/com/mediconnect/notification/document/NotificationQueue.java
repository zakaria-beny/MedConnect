package com.mediconnect.notification.document;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "notification_queue")
public class NotificationQueue {
    @Id
    private String id;
    private String userId;
    private String type;
    private String title;
    private String content;
    private List<String> channels;
    private String status; // PENDING, PROCESSING, SENT, FAILED
    private int attempts = 0;
    private int maxAttempts = 3;
    private boolean urgent = false;
    private LocalDateTime scheduledAt;
    private LocalDateTime processedAt;
    private LocalDateTime createdAt;
    private String eventType;
    private String resourceId;
}