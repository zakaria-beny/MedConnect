package com.medconnect.teleconsulation.dto.response;

import com.medconnect.teleconsulation.model.SessionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionResponse {
    private String id;
    private String sessionId;
    private String appointmentId;
    private String doctorId;
    private String patientId;
    private SessionStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime startedAt;
    private LocalDateTime endedAt;
    private Long durationSeconds;
    private boolean screenSharing;
    private boolean recording;
    private String recordingId;
}
