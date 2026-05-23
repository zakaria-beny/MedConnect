package com.medconnect.teleconsulation.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "video_sessions")
public class VideoSession {

    @Id
    private String id;

    @Indexed(unique = true)
    private String sessionId;

    private String appointmentId;
    private String doctorId;
    private String patientId;
    private SessionStatus status;
    private String encryptionKey;

    private LocalDateTime createdAt;
    private LocalDateTime startedAt;
    private LocalDateTime endedAt;
    private Long durationSeconds;

    private boolean screenSharing;
    private LocalDateTime screenShareStartedAt;
    private Long screenShareDurationSeconds;

    private boolean recording;
    private String recordingId;
}
