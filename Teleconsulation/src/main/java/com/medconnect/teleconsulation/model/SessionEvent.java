package com.medconnect.teleconsulation.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "session_events")
public class SessionEvent {

    @Id
    private String id;

    private String sessionId;
    private String eventType;
    private String userId;
    private LocalDateTime timestamp;
    private String details;
}
