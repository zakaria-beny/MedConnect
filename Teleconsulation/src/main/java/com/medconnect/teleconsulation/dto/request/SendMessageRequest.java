package com.medconnect.teleconsulation.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SendMessageRequest {

    @NotBlank(message = "Sender ID is required")
    private String senderId;

    @NotBlank(message = "Message cannot be blank")
    private String message;
}
