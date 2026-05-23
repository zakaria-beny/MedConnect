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
@Document(collection = "delivery_logs")
public class DeliveryLog {
    @Id
    private String id;
    private String notificationId;
    private String userId;
    private String channel;
    private String status;
    private String errorMessage;
    private int attempts = 0;
    private LocalDateTime lastAttemptAt;
    private LocalDateTime deliveredAt;
}