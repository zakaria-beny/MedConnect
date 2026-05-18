package com.medconnect.teleconsulation.dto.response;

import com.medconnect.teleconsulation.model.ChatMessage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatResponse {
    private String sessionId;
    private List<ChatMessage> messages;
    private LocalDateTime lastUpdated;
}
