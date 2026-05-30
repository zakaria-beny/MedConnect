package com.medconnect.teleconsulation.service;

import com.medconnect.teleconsulation.dto.request.CreateSessionRequest;
import com.medconnect.teleconsulation.dto.response.JoinLinkResponse;
import com.medconnect.teleconsulation.dto.response.SessionResponse;
import com.medconnect.teleconsulation.dto.response.SessionStatusResponse;
import com.medconnect.teleconsulation.exception.SessionAlreadyEndedException;
import com.medconnect.teleconsulation.exception.SessionNotFoundException;
import com.medconnect.teleconsulation.kafka.IKafkaEventService;
import com.medconnect.teleconsulation.model.SessionStatus;
import com.medconnect.teleconsulation.model.VideoSession;
import com.medconnect.teleconsulation.repository.SessionEventRepository;
import com.medconnect.teleconsulation.repository.SessionParticipantRepository;
import com.medconnect.teleconsulation.repository.VideoSessionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VideoSessionServiceTest {

    @Mock VideoSessionRepository videoSessionRepository;
    @Mock SessionParticipantRepository participantRepository;
    @Mock SessionEventRepository sessionEventRepository;
    @Mock IKafkaEventService kafkaEventService;

    private VideoSessionService service;

    private VideoSession activeSession;
    private VideoSession createdSession;
    private VideoSession endedSession;

    @BeforeEach
    void setUp() {
        createdSession = VideoSession.builder()
                .id("mongo-id-1")
                .sessionId("sess-001")
                .appointmentId("appt-001")
                .doctorId("doc-001")
                .patientId("pat-001")
                .status(SessionStatus.CREATED)
                .createdAt(LocalDateTime.now())
                .build();

        activeSession = VideoSession.builder()
                .id("mongo-id-2")
                .sessionId("sess-002")
                .doctorId("doc-001")
                .patientId("pat-001")
                .status(SessionStatus.ACTIVE)
                .startedAt(LocalDateTime.now().minusMinutes(5))
                .build();

        endedSession = VideoSession.builder()
                .id("mongo-id-3")
                .sessionId("sess-003")
                .doctorId("doc-001")
                .patientId("pat-001")
                .status(SessionStatus.ENDED)
                .build();

        service = new VideoSessionService(
                videoSessionRepository,
                participantRepository,
                sessionEventRepository,
                new EncryptionService(),
                kafkaEventService
        );
    }

    // ─── createSession ────────────────────────────────────────────────────────

    @Nested
    @DisplayName("createSession")
    class CreateSession {

        @Test
        @DisplayName("Happy path: valid request creates session with CREATED status")
        void happyPath() {
            when(videoSessionRepository.save(any())).thenAnswer(inv -> {
                VideoSession s = inv.getArgument(0);
                s.setId("new-id");
                return s;
            });
            when(sessionEventRepository.save(any())).thenReturn(null);

            CreateSessionRequest req = new CreateSessionRequest();
            req.setAppointmentId("appt-001");
            req.setDoctorId("doc-001");
            req.setPatientId("pat-001");

            SessionResponse resp = service.createSession(req);

            assertThat(resp.getStatus()).isEqualTo(SessionStatus.CREATED);
            assertThat(resp.getDoctorId()).isEqualTo("doc-001");
            assertThat(resp.getPatientId()).isEqualTo("pat-001");
            assertThat(resp.getAppointmentId()).isEqualTo("appt-001");
            assertThat(resp.getSessionId()).isNotBlank();

            verify(videoSessionRepository).save(any());
            verify(kafkaEventService).publish(eq("teleconsult.session.created"), anyString());
        }
    }

    // ─── startSession ─────────────────────────────────────────────────────────

    @Nested
    @DisplayName("startSession")
    class StartSession {

        @Test
        @DisplayName("Happy path: CREATED → ACTIVE")
        void happyPath() {
            when(videoSessionRepository.findBySessionId("sess-001")).thenReturn(Optional.of(createdSession));
            when(videoSessionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
            when(sessionEventRepository.save(any())).thenReturn(null);

            SessionResponse resp = service.startSession("sess-001");

            assertThat(resp.getStatus()).isEqualTo(SessionStatus.ACTIVE);
            assertThat(resp.getStartedAt()).isNotNull();
        }

        @Test
        @DisplayName("Error: ACTIVE session → 409 conflict")
        void alreadyActive() {
            when(videoSessionRepository.findBySessionId("sess-002")).thenReturn(Optional.of(activeSession));

            assertThatThrownBy(() -> service.startSession("sess-002"))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("already active");
        }

        @Test
        @DisplayName("Error: ENDED session → SessionAlreadyEndedException")
        void alreadyEnded() {
            when(videoSessionRepository.findBySessionId("sess-003")).thenReturn(Optional.of(endedSession));

            assertThatThrownBy(() -> service.startSession("sess-003"))
                    .isInstanceOf(SessionAlreadyEndedException.class)
                    .hasMessageContaining("already ended");
        }

        @Test
        @DisplayName("Error: nonexistent session → 404")
        void notFound() {
            when(videoSessionRepository.findBySessionId("missing")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.startSession("missing"))
                    .isInstanceOf(SessionNotFoundException.class);
        }
    }

    // ─── generateJoinLink ─────────────────────────────────────────────────────

    @Nested
    @DisplayName("generateJoinLink")
    class GenerateJoinLink {

        @Test
        @DisplayName("Happy path: role=DOCTOR")
        void roleDoctor() {
            when(videoSessionRepository.findBySessionId("sess-001")).thenReturn(Optional.of(createdSession));

            JoinLinkResponse resp = service.generateJoinLink("sess-001", "DOCTOR");

            assertThat(resp.getRole()).isEqualTo("DOCTOR");
            assertThat(resp.getJoinLink()).contains("sess-001");
            assertThat(resp.getExpiresAt()).isAfter(LocalDateTime.now());
        }

        @Test
        @DisplayName("Happy path: role=PATIENT (case insensitive)")
        void rolePatientCaseInsensitive() {
            when(videoSessionRepository.findBySessionId("sess-001")).thenReturn(Optional.of(createdSession));

            JoinLinkResponse resp = service.generateJoinLink("sess-001", "patient");

            assertThat(resp.getRole()).isEqualTo("patient");
        }

        @Test
        @DisplayName("Error: invalid role → 400")
        void invalidRole() {
            assertThatThrownBy(() -> service.generateJoinLink("sess-001", "ADMIN"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Invalid role");
        }

        @Test
        @DisplayName("Edge case: blank role → 400")
        void blankRole() {
            assertThatThrownBy(() -> service.generateJoinLink("sess-001", ""))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Error: session not found → 404")
        void sessionNotFound() {
            when(videoSessionRepository.findBySessionId("bad-id")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.generateJoinLink("bad-id", "DOCTOR"))
                    .isInstanceOf(SessionNotFoundException.class);
        }
    }

    // ─── getSessionStatus ─────────────────────────────────────────────────────

    @Nested
    @DisplayName("getSessionStatus")
    class GetSessionStatus {

        @Test
        @DisplayName("Happy path: returns status with participant count and duration")
        void happyPath() {
            when(videoSessionRepository.findBySessionId("sess-002")).thenReturn(Optional.of(activeSession));
            when(participantRepository.countBySessionId("sess-002")).thenReturn(2L);

            SessionStatusResponse resp = service.getSessionStatus("sess-002");

            assertThat(resp.getStatus()).isEqualTo(SessionStatus.ACTIVE);
            assertThat(resp.getParticipantCount()).isEqualTo(2L);
            assertThat(resp.getDurationSeconds()).isGreaterThanOrEqualTo(0L);
        }

        @Test
        @DisplayName("Error: session not found → 404")
        void notFound() {
            when(videoSessionRepository.findBySessionId("x")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.getSessionStatus("x"))
                    .isInstanceOf(SessionNotFoundException.class);
        }
    }
}
