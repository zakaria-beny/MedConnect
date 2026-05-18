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
@Document(collection = "recordings")
public class Recording {

    @Id
    private String id;

    private String sessionId;
    private boolean consentGiven;
    private LocalDateTime startedAt;
    private LocalDateTime stoppedAt;
    private Long durationSeconds;
    private String storedPath;
    private LocalDateTime expiresAt;
    private boolean deleted;
}
