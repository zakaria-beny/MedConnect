package com.medconnect.teleconsulation.service;

import com.medconnect.teleconsulation.exception.SessionNotFoundException;
import com.medconnect.teleconsulation.model.SessionEvent;
import com.medconnect.teleconsulation.model.SessionStatus;
import com.medconnect.teleconsulation.model.VideoSession;
import com.medconnect.teleconsulation.repository.SessionEventRepository;
import com.medconnect.teleconsulation.repository.VideoSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class NetworkRecoveryService {

    private static final int MAX_RECONNECT_ATTEMPTS = 5;
    private static final int CONNECTION_TIMEOUT_SECONDS = 5;

    private final VideoSessionRepository videoSessionRepository;
    private final SessionEventRepository sessionEventRepository;

    public Map<String, Object> detectConnectionLoss(String sessionId) {
        getSession(sessionId);
        logEvent(sessionId, "CONNECTION_LOSS_DETECTED",
                "Connection loss detected after " + CONNECTION_TIMEOUT_SECONDS + " seconds of inactivity");
        return Map.of(
                "sessionId", sessionId,
                "status", "connection_lost",
                "timeoutSeconds", CONNECTION_TIMEOUT_SECONDS,
                "detectedAt", LocalDateTime.now().toString()
        );
    }

    public Map<String, Object> triggerAutoReconnect(String sessionId) {
        getSession(sessionId);
        logEvent(sessionId, "AUTO_RECONNECT_TRIGGERED",
                "Auto-reconnect triggered, max attempts: " + MAX_RECONNECT_ATTEMPTS);
        return Map.of(
                "sessionId", sessionId,
                "status", "reconnecting",
                "maxAttempts", MAX_RECONNECT_ATTEMPTS,
                "message", "Attempting to restore connection"
        );
    }

    public Map<String, Object> offlineQueue(String sessionId) {
        getSession(sessionId);
        logEvent(sessionId, "OFFLINE_QUEUE_ACTIVE", "Buffering session data during network outage");
        return Map.of(
                "sessionId", sessionId,
                "status", "queuing",
                "message", "Data is being buffered. Session will resume automatically."
        );
    }

    public Map<String, Object> resumeSession(String sessionId) {
        VideoSession session = getSession(sessionId);
        if (session.getStatus() != SessionStatus.ACTIVE) {
            session.setStatus(SessionStatus.ACTIVE);
            videoSessionRepository.save(session);
        }
        logEvent(sessionId, "SESSION_RESUMED", "Session successfully resumed after network recovery");
        return Map.of("sessionId", sessionId, "status", "resumed",
                "message", "Connection restored. Session is active.");
    }

    public Map<String, Object> fallbackToPhone(String sessionId) {
        VideoSession session = getSession(sessionId);
        logEvent(sessionId, "FALLBACK_TO_PHONE",
                "Video connection failed permanently. Patient instructed to call doctor.");
        return Map.of(
                "sessionId", sessionId,
                "status", "fallback_to_phone",
                "doctorId", session.getDoctorId(),
                "message", "Video call unavailable. Please contact your doctor by phone."
        );
    }

    private VideoSession getSession(String sessionId) {
        return videoSessionRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new SessionNotFoundException("Session not found: " + sessionId));
    }

    private void logEvent(String sessionId, String eventType, String details) {
        sessionEventRepository.save(SessionEvent.builder()
                .sessionId(sessionId)
                .eventType(eventType)
                .timestamp(LocalDateTime.now())
                .details(details)
                .build());
    }
}
