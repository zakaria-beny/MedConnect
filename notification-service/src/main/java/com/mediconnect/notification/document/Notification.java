package com.mediconnect.notification.document;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "notification_history")
public class Notification {
    @Id
    private String id;
    private String userId;
    private String type;
    private String title;
    private String content;
    private String channel;
    private String status;
    private boolean read = false;
    private LocalDateTime createdAt;
    private LocalDateTime sentAt;
    private LocalDateTime readAt;
    private String resourceId;
    private String eventType;
}