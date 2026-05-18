package com.medconnect.teleconsulation.dto.response;

import com.medconnect.teleconsulation.model.SessionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionStatusResponse {
    private String sessionId;
    private SessionStatus status;
    private long participantCount;
    private Long durationSeconds;
}
