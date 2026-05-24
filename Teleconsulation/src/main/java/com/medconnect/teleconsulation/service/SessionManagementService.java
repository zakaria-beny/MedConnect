package com.medconnect.teleconsulation.service;

import com.medconnect.teleconsulation.dto.response.SessionResponse;
import com.medconnect.teleconsulation.exception.SessionAlreadyEndedException;
import com.medconnect.teleconsulation.exception.SessionNotFoundException;
import com.medconnect.teleconsulation.kafka.KafkaEventService;
import com.medconnect.teleconsulation.model.SessionEvent;
import com.medconnect.teleconsulation.model.SessionStatus;
import com.medconnect.teleconsulation.model.VideoSession;
import com.medconnect.teleconsulation.repository.SessionEventRepository;
import com.medconnect.teleconsulation.repository.VideoSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SessionManagementService {

    private final VideoSessionRepository videoSessionRepository;
    private final SessionEventRepository sessionEventRepository;
    private final KafkaEventService kafkaEventService;
    private final VideoSessionService videoSessionService;
    private final RecordingService recordingService;

    public SessionResponse endSession(String sessionId) {
        VideoSession session = getSession(sessionId);
        if (session.getStatus() == SessionStatus.ENDED || session.getStatus() == SessionStatus.FORCE_ENDED) {
            throw new SessionAlreadyEndedException("Session already ended: " + sessionId);
        }
        if (session.isRecording()) {
            recordingService.stopRecording(sessionId);
        }
        LocalDateTime now = LocalDateTime.now();
        session.setStatus(SessionStatus.ENDED);
        session.setEndedAt(now);
        if (session.getStartedAt() != null) {
            session.setDurationSeconds(ChronoUnit.SECONDS.between(session.getStartedAt(), now));
        }
        session = videoSessionRepository.save(session);
        logEvent(sessionId, "SESSION_ENDED", null, "Session ended gracefully");
        kafkaEventService.publish("teleconsult.session.ended", sessionId);
        return videoSessionService.toResponse(session);
    }

    public SessionResponse forceEndSession(String sessionId) {
        VideoSession session = getSession(sessionId);
        LocalDateTime now = LocalDateTime.now();
        session.setStatus(SessionStatus.FORCE_ENDED);
        session.setEndedAt(now);
        if (session.getStartedAt() != null) {
            session.setDurationSeconds(ChronoUnit.SECONDS.between(session.getStartedAt(), now));
        }
        session = videoSessionRepository.save(session);
        logEvent(sessionId, "SESSION_FORCE_ENDED", "admin", "Session force-ended by administrator");
        kafkaEventService.publish("teleconsult.session.force_ended", sessionId);
        return videoSessionService.toResponse(session);
    }

    public Long calculateSessionDuration(String sessionId) {
        VideoSession session = getSession(sessionId);
        if (session.getStartedAt() == null) return 0L;
        LocalDateTime end = session.getEndedAt() != null ? session.getEndedAt() : LocalDateTime.now();
        return ChronoUnit.SECONDS.between(session.getStartedAt(), end);
    }

    public Map<String, Object> generateSessionSummary(String sessionId) {
        VideoSession session = getSession(sessionId);
        Long duration = calculateSessionDuration(sessionId);
        List<SessionEvent> events = sessionEventRepository.findBySessionIdOrderByTimestampDesc(sessionId);
        return Map.of(
                "sessionId", sessionId,
                "appointmentId", session.getAppointmentId() != null ? session.getAppointmentId() : "",
                "doctorId", session.getDoctorId(),
                "patientId", session.getPatientId(),
                "status", session.getStatus().name(),
                "durationSeconds", duration,
                "totalEvents", events.size(),
                "startedAt", session.getStartedAt() != null ? session.getStartedAt().toString() : "N/A",
                "endedAt", session.getEndedAt() != null ? session.getEndedAt().toString() : "N/A"
        );
    }

    public void trackSessionMetrics(String sessionId) {
        Long duration = calculateSessionDuration(sessionId);
        logEvent(sessionId, "METRICS_TRACKED", null,
                "Session metrics: duration=" + duration + "s");
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
}
