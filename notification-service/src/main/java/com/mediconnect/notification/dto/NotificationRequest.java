package com.mediconnect.notification.dto;

import lombok.Data;
import java.util.List;

@Data
public class NotificationRequest {
    private String userId;
    private String type;
    private String title;
    private String content;
    private List<String> channels;
    private String resourceId;
    private String eventType;
}