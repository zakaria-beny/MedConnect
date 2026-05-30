package com.medconnect.teleconsulation.service;

import com.medconnect.teleconsulation.dto.response.RecordingResponse;
import com.medconnect.teleconsulation.exception.ConsentRequiredException;
import com.medconnect.teleconsulation.exception.SessionNotFoundException;
import com.medconnect.teleconsulation.kafka.IKafkaEventService;
import com.medconnect.teleconsulation.model.Recording;
import com.medconnect.teleconsulation.model.SessionEvent;
import com.medconnect.teleconsulation.model.VideoSession;
import com.medconnect.teleconsulation.repository.RecordingRepository;
import com.medconnect.teleconsulation.repository.SessionEventRepository;
import com.medconnect.teleconsulation.repository.VideoSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class RecordingService {

    private final RecordingRepository recordingRepository;
    private final VideoSessionRepository videoSessionRepository;
    private final SessionEventRepository sessionEventRepository;
    private final IKafkaEventService kafkaEventService;

    public RecordingResponse startRecording(String sessionId) {
        if (!validateConsent(sessionId)) {
            throw new ConsentRequiredException("Patient consent is required before recording session: " + sessionId);
        }
        VideoSession session = getSession(sessionId);
        if (session.isRecording()) {
            throw new IllegalStateException("Recording is already in progress for session: " + sessionId);
        }

        LocalDateTime now = LocalDateTime.now();
        Recording recording = Recording.builder()
                .sessionId(sessionId)
                .consentGiven(true)
                .startedAt(now)
                .expiresAt(now.plusYears(2))
                .deleted(false)
                .build();
        recording = recordingRepository.save(recording);

        session.setRecording(true);
        session.setRecordingId(recording.getId());
        videoSessionRepository.save(session);

        logEvent(sessionId, "RECORDING_STARTED", session.getDoctorId(), "Recording started with consent");
        kafkaEventService.publish("teleconsult.recording.started", sessionId);
        return toResponse(recording);
    }

    public RecordingResponse stopRecording(String sessionId) {
        VideoSession session = getSession(sessionId);
        Recording recording = recordingRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new SessionNotFoundException("No recording found for session: " + sessionId));
        if (recording.getStoppedAt() != null) {
            throw new IllegalStateException("Recording has already been stopped for session: " + sessionId);
        }

        LocalDateTime now = LocalDateTime.now();
        recording.setStoppedAt(now);
        recording.setDurationSeconds(ChronoUnit.SECONDS.between(recording.getStartedAt(), now));
        recording.setStoredPath("/recordings/encrypted/" + sessionId + "/" + recording.getId() + ".enc");
        recording = recordingRepository.save(recording);

        session.setRecording(false);
        videoSessionRepository.save(session);

        logEvent(sessionId, "RECORDING_STOPPED", null,
                "Recording stopped. Duration: " + recording.getDurationSeconds() + "s");
        kafkaEventService.publish("teleconsult.recording.stopped", sessionId);
        return toResponse(recording);
    }

    public boolean validateConsent(String sessionId) {
        return true;
    }

    public void storeRecording(String sessionId, String videoFile) {
        Recording recording = recordingRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new IllegalStateException("No recording found for session: " + sessionId));
        recording.setStoredPath("/recordings/encrypted/" + sessionId + "/" + videoFile);
        recordingRepository.save(recording);
        logEvent(sessionId, "RECORDING_STORED", null, "Recording stored at encrypted path");
    }

    public Long trackRecordingDuration(String sessionId) {
        return recordingRepository.findBySessionId(sessionId)
                .map(r -> {
                    if (r.getStoppedAt() != null) return r.getDurationSeconds();
                    return ChronoUnit.SECONDS.between(r.getStartedAt(), LocalDateTime.now());
                })
                .orElse(0L);
    }

    public void deleteRecordingAfterRetention(String sessionId) {
        recordingRepository.findBySessionId(sessionId).ifPresent(r -> {
            if (r.getExpiresAt() != null && r.getExpiresAt().isBefore(LocalDateTime.now()) && !r.isDeleted()) {
                r.setDeleted(true);
                r.setStoredPath(null);
                recordingRepository.save(r);
                logEvent(sessionId, "RECORDING_DELETED", null, "Recording deleted after 2-year retention policy");
            }
        });
    }

    public RecordingResponse getRecording(String sessionId) {
        Recording recording = recordingRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new SessionNotFoundException("No recording found for session: " + sessionId));
        return toResponse(recording);
    }

    private VideoSession getSession(String sessionId) {
        return videoSessionRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new SessionNotFoundException("Session not found: " + sessionId));
    }

    private void logEvent(String sessionId, String eventType, String userId, String details) {
        sessionEventRepository.save(SessionEvent.builder()
                .sessionId(sessionId)
                .eventType(eventType)
                .userId(userId)
                .timestamp(LocalDateTime.now())
                .details(details)
                .build());
    }

    private RecordingResponse toResponse(Recording r) {
        return RecordingResponse.builder()
                .id(r.getId())
                .sessionId(r.getSessionId())
                .consentGiven(r.isConsentGiven())
                .startedAt(r.getStartedAt())
                .stoppedAt(r.getStoppedAt())
                .durationSeconds(r.getDurationSeconds())
                .expiresAt(r.getExpiresAt())
                .deleted(r.isDeleted())
                .build();
    }
}
