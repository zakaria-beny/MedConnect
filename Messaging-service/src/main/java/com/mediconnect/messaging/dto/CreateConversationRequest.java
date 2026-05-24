package com.mediconnect.messaging.dto;

import lombok.Data;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

@Data
public class CreateConversationRequest {
    @NotEmpty(message = "Il faut au moins 2 participants")
    private List<String> participants;
    private String type = "ONE_TO_ONE";
}