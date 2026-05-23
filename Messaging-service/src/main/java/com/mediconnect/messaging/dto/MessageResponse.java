package com.mediconnect.messaging.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class MessageResponse {
    private String id;
    private String conversationId;
    private String senderId;
    private String content;
    private LocalDateTime sentAt;
    private boolean edited;
    private String messageType;
}