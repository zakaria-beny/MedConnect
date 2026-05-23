package com.medconnect.teleconsulation.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "session_chats")
public class SessionChat {

    @Id
    private String id;

    private String sessionId;

    @Builder.Default
    private List<ChatMessage> messages = new ArrayList<>();

    private LocalDateTime lastUpdated;
}
