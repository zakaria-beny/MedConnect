package com.medconnect.teleconsulation.service;

import com.medconnect.teleconsulation.dto.response.SessionResponse;
import com.medconnect.teleconsulation.exception.SessionNotFoundException;
import com.medconnect.teleconsulation.model.SessionEvent;
import com.medconnect.teleconsulation.model.VideoSession;
import com.medconnect.teleconsulation.repository.SessionEventRepository;
import com.medconnect.teleconsulation.repository.VideoSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ScreenShareService {

    private final VideoSessionRepository videoSessionRepository;
    private final SessionEventRepository sessionEventRepository;
    private final VideoSessionService videoSessionService;

    public SessionResponse startScreenShare(String sessionId, String doctorId) {
        VideoSession session = getSession(sessionId);
        if (!doctorId.equals(session.getDoctorId())) {
            throw new IllegalStateException("Only the doctor can start screen sharing in session: " + sessionId);
        }
        if (session.isScreenSharing()) {
            throw new IllegalStateException("Screen sharing is already active for session: " + sessionId);
        }
        session.setScreenSharing(true);
        session.setScreenShareStartedAt(LocalDateTime.now());
        session = videoSessionRepository.save(session);
        logEvent(sessionId, "SCREEN_SHARE_STARTED", doctorId, "Doctor started screen sharing");
        return videoSessionService.toResponse(session);
    }

    public SessionResponse stopScreenShare(String sessionId) {
        VideoSession session = getSession(sessionId);
        long duration = 0;
        if (session.getScreenShareStartedAt() != null) {
            duration = ChronoUnit.SECONDS.between(session.getScreenShareStartedAt(), LocalDateTime.now());
        }
        session.setScreenSharing(false);
        session.setScreenShareDurationSeconds(duration);
        session = videoSessionRepository.save(session);
        logEvent(sessionId, "SCREEN_SHARE_STOPPED", null,
                "Screen sharing stopped. Duration: " + duration + "s");
        return videoSessionService.toResponse(session);
    }

    public long trackScreenShareDuration(String sessionId) {
        VideoSession session = getSession(sessionId);
        if (session.getScreenShareStartedAt() == null) return 0L;
        return session.isScreenSharing()
                ? ChronoUnit.SECONDS.between(session.getScreenShareStartedAt(), LocalDateTime.now())
                : (session.getScreenShareDurationSeconds() != null ? session.getScreenShareDurationSeconds() : 0L);
    }

    public Map<String, Object> shareImage(String sessionId, String imageData) {
        if (imageData == null || imageData.isBlank()) {
            throw new IllegalArgumentException("Image data cannot be null or empty");
        }
        getSession(sessionId);
        logEvent(sessionId, "MEDICAL_IMAGE_SHARED", null,
                "Medical image shared in session. Size: " + imageData.length() + " bytes");
        return Map.of(
                "sessionId", sessionId,
                "status", "shared",
                "imageSize", imageData.length(),
                "timestamp", LocalDateTime.now().toString()
        );
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
