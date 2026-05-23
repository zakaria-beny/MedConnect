package com.medconnect.teleconsulation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecordingResponse {
    private String id;
    private String sessionId;
    private boolean consentGiven;
    private LocalDateTime startedAt;
    private LocalDateTime stoppedAt;
    private Long durationSeconds;
    private LocalDateTime expiresAt;
    private boolean deleted;
}
