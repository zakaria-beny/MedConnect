package com.mediconnect.messaging.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;

@Data
public class SendMessageRequest {
    @NotBlank
    private String conversationId;
    @NotBlank
    private String senderId;
    @NotBlank
    private String content;
    private String messageType = "TEXT";
}