package com.medconnect.teleconsulation.service;

import com.medconnect.teleconsulation.dto.request.CreateSessionRequest;
import com.medconnect.teleconsulation.dto.response.JoinLinkResponse;
import com.medconnect.teleconsulation.dto.response.SessionResponse;
import com.medconnect.teleconsulation.dto.response.SessionStatusResponse;
import com.medconnect.teleconsulation.exception.SessionAlreadyEndedException;
import com.medconnect.teleconsulation.exception.SessionNotFoundException;
import com.medconnect.teleconsulation.kafka.IKafkaEventService;
import com.medconnect.teleconsulation.model.SessionEvent;
import com.medconnect.teleconsulation.model.SessionStatus;
import com.medconnect.teleconsulation.model.VideoSession;
import com.medconnect.teleconsulation.repository.SessionEventRepository;
import com.medconnect.teleconsulation.repository.SessionParticipantRepository;
import com.medconnect.teleconsulation.repository.VideoSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VideoSessionService {

    private final VideoSessionRepository videoSessionRepository;
    private final SessionParticipantRepository participantRepository;
    private final SessionEventRepository sessionEventRepository;
    private final EncryptionService encryptionService;
    private final IKafkaEventService kafkaEventService;

    public SessionResponse createSession(CreateSessionRequest request) {
        return videoSessionRepository.findByAppointmentId(request.getAppointmentId())
                .map(this::toResponse)
                .orElseGet(() -> createNewSession(request));
    }

    private SessionResponse createNewSession(CreateSessionRequest request) {
        String sessionId = UUID.randomUUID().toString();
        String encryptionKey = encryptionService.generateSessionKey();

        VideoSession session = VideoSession.builder()
                .sessionId(sessionId)
                .appointmentId(request.getAppointmentId())
                .doctorId(request.getDoctorId())
                .patientId(request.getPatientId())
                .status(SessionStatus.CREATED)
                .encryptionKey(encryptionKey)
                .createdAt(LocalDateTime.now())
                .screenSharing(false)
                .recording(false)
                .build();

        session = videoSessionRepository.save(session);
        logEvent(sessionId, "SESSION_CREATED", request.getDoctorId(),
                "Session created for appointment: " + request.getAppointmentId());
        kafkaEventService.publish("teleconsult.session.created", sessionId);
        return toResponse(session);
    }

    public JoinLinkResponse generateJoinLink(String sessionId, String userRole) {
        if (!"DOCTOR".equalsIgnoreCase(userRole) && !"PATIENT".equalsIgnoreCase(userRole)) {
            throw new IllegalArgumentException(
                    "Invalid role '" + userRole + "'. Allowed values: DOCTOR, PATIENT");
        }
        getBySessionId(sessionId);
        String token = Base64.getEncoder().encodeToString(
                (sessionId + ":" + userRole + ":" + System.currentTimeMillis()).getBytes()
        );
        String link = "/api/teleconsult/sessions/" + sessionId + "/join?token=" + token + "&role=" + userRole;
        return JoinLinkResponse.builder()
                .sessionId(sessionId)
                .joinLink(link)
                .role(userRole)
                .expiresAt(LocalDateTime.now().plusHours(2))
                .build();
    }

    public SessionResponse startSession(String sessionId) {
        VideoSession session = getBySessionId(sessionId);
        if (session.getStatus() == SessionStatus.ENDED || session.getStatus() == SessionStatus.FORCE_ENDED) {
            throw new SessionAlreadyEndedException("Session already ended: " + sessionId);
        }
        if (session.getStatus() == SessionStatus.ACTIVE) {
            throw new IllegalStateException("Session is already active: " + sessionId);
        }
        session.setStatus(SessionStatus.ACTIVE);
        session.setStartedAt(LocalDateTime.now());
        session = videoSessionRepository.save(session);

        encryptionService.initiateDTLSHandshake(sessionId);
        logEvent(sessionId, "SESSION_STARTED", session.getDoctorId(), "Doctor initiated the session");
        kafkaEventService.publish("teleconsult.session.started", sessionId);
        return toResponse(session);
    }

    public SessionStatusResponse getSessionStatus(String sessionId) {
        VideoSession session = getBySessionId(sessionId);
        long participantCount = participantRepository.countBySessionId(sessionId);
        Long duration = null;
        if (session.getStartedAt() != null) {
            LocalDateTime end = session.getEndedAt() != null ? session.getEndedAt() : LocalDateTime.now();
            duration = ChronoUnit.SECONDS.between(session.getStartedAt(), end);
        }
        return SessionStatusResponse.builder()
                .sessionId(sessionId)
                .status(session.getStatus())
                .participantCount(participantCount)
                .durationSeconds(duration)
                .build();
    }

    public SessionResponse getSession(String sessionId) {
        return toResponse(getBySessionId(sessionId));
    }

    public VideoSession getBySessionId(String sessionId) {
        return videoSessionRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new SessionNotFoundException("Session not found: " + sessionId));
    }

    public SessionResponse toResponse(VideoSession session) {
        return SessionResponse.builder()
                .id(session.getId())
                .sessionId(session.getSessionId())
                .appointmentId(session.getAppointmentId())
                .doctorId(session.getDoctorId())
                .patientId(session.getPatientId())
                .status(session.getStatus())
                .createdAt(session.getCreatedAt())
                .startedAt(session.getStartedAt())
                .endedAt(session.getEndedAt())
                .durationSeconds(session.getDurationSeconds())
                .screenSharing(session.isScreenSharing())
                .recording(session.isRecording())
                .recordingId(session.getRecordingId())
                .build();
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
