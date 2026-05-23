package com.medconnect.teleconsulation.service;

import com.medconnect.teleconsulation.dto.response.RecordingResponse;
import com.medconnect.teleconsulation.exception.ConsentRequiredException;
import com.medconnect.teleconsulation.exception.SessionNotFoundException;
import com.medconnect.teleconsulation.kafka.KafkaEventService;
import com.medconnect.teleconsulation.model.Recording;
import com.medconnect.teleconsulation.model.SessionStatus;
import com.medconnect.teleconsulation.model.VideoSession;
import com.medconnect.teleconsulation.repository.RecordingRepository;
import com.medconnect.teleconsulation.repository.SessionEventRepository;
import com.medconnect.teleconsulation.repository.VideoSessionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecordingServiceTest {

    @Mock RecordingRepository recordingRepository;
    @Mock VideoSessionRepository videoSessionRepository;
    @Mock SessionEventRepository sessionEventRepository;
    @Mock KafkaEventService kafkaEventService;

    @InjectMocks RecordingService service;

    private VideoSession activeSession;
    private Recording activeRecording;
    private Recording stoppedRecording;

    @BeforeEach
    void setUp() {
        activeSession = VideoSession.builder()
                .id("mongo-1")
                .sessionId("sess-001")
                .doctorId("doc-001")
                .status(SessionStatus.ACTIVE)
                .recording(false)
                .build();

        activeRecording = Recording.builder()
                .id("rec-001")
                .sessionId("sess-001")
                .consentGiven(true)
                .startedAt(LocalDateTime.now().minusMinutes(5))
                .expiresAt(LocalDateTime.now().plusYears(2))
                .deleted(false)
                .build();

        stoppedRecording = Recording.builder()
                .id("rec-002")
                .sessionId("sess-001")
                .consentGiven(true)
                .startedAt(LocalDateTime.now().minusMinutes(10))
                .stoppedAt(LocalDateTime.now().minusMinutes(1))
                .durationSeconds(540L)
                .deleted(false)
                .build();
    }

    // ─── startRecording ───────────────────────────────────────────────────────

    @Nested
    @DisplayName("startRecording")
    class StartRecording {

        @Test
        @DisplayName("Happy path: starts recording with consent")
        void happyPath() {
            when(videoSessionRepository.findBySessionId("sess-001")).thenReturn(Optional.of(activeSession));
            when(recordingRepository.save(any())).thenAnswer(inv -> {
                Recording r = inv.getArgument(0);
                r.setId("rec-new");
                return r;
            });
            when(sessionEventRepository.save(any())).thenReturn(null);

            RecordingResponse resp = service.startRecording("sess-001");

            assertThat(resp.isConsentGiven()).isTrue();
            assertThat(resp.getStartedAt()).isNotNull();
            assertThat(resp.getStoppedAt()).isNull();
            verify(kafkaEventService).publish(eq("teleconsult.recording.started"), anyString());
        }

        @Test
        @DisplayName("Error: recording already in progress → 409")
        void alreadyRecording() {
            VideoSession recordingSession = VideoSession.builder()
                    .sessionId("sess-001").status(SessionStatus.ACTIVE).recording(true).build();
            when(videoSessionRepository.findBySessionId("sess-001")).thenReturn(Optional.of(recordingSession));

            assertThatThrownBy(() -> service.startRecording("sess-001"))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("already in progress");
        }

        @Test
        @DisplayName("Error: session not found → 404")
        void sessionNotFound() {
            when(videoSessionRepository.findBySessionId("x")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.startRecording("x"))
                    .isInstanceOf(SessionNotFoundException.class);
        }
    }

    // ─── stopRecording ────────────────────────────────────────────────────────

    @Nested
    @DisplayName("stopRecording")
    class StopRecording {

        @Test
        @DisplayName("Happy path: stops recording and stores duration")
        void happyPath() {
            VideoSession recordingSession = VideoSession.builder()
                    .sessionId("sess-001").status(SessionStatus.ACTIVE).recording(true).build();
            when(videoSessionRepository.findBySessionId("sess-001")).thenReturn(Optional.of(recordingSession));
            when(recordingRepository.findBySessionId("sess-001")).thenReturn(Optional.of(activeRecording));
            when(recordingRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
            when(videoSessionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
            when(sessionEventRepository.save(any())).thenReturn(null);

            RecordingResponse resp = service.stopRecording("sess-001");

            assertThat(resp.getStoppedAt()).isNotNull();
            assertThat(resp.getDurationSeconds()).isGreaterThan(0L);
            verify(kafkaEventService).publish(eq("teleconsult.recording.stopped"), anyString());
        }

        @Test
        @DisplayName("Error: recording already stopped → 409 (double-stop guard)")
        void doubleStop() {
            when(videoSessionRepository.findBySessionId("sess-001")).thenReturn(Optional.of(activeSession));
            when(recordingRepository.findBySessionId("sess-001")).thenReturn(Optional.of(stoppedRecording));

            assertThatThrownBy(() -> service.stopRecording("sess-001"))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("already been stopped");
        }

        @Test
        @DisplayName("Error: no recording found → 404")
        void noRecordingFound() {
            when(videoSessionRepository.findBySessionId("sess-001")).thenReturn(Optional.of(activeSession));
            when(recordingRepository.findBySessionId("sess-001")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.stopRecording("sess-001"))
                    .isInstanceOf(SessionNotFoundException.class);
        }
    }

    // ─── getRecording ─────────────────────────────────────────────────────────

    @Nested
    @DisplayName("getRecording")
    class GetRecording {

        @Test
        @DisplayName("Happy path: returns recording metadata")
        void happyPath() {
            when(recordingRepository.findBySessionId("sess-001")).thenReturn(Optional.of(stoppedRecording));

            RecordingResponse resp = service.getRecording("sess-001");

            assertThat(resp.getId()).isEqualTo("rec-002");
            assertThat(resp.getDurationSeconds()).isEqualTo(540L);
        }

        @Test
        @DisplayName("Error: no recording → 404 (not 409)")
        void notFound() {
            when(recordingRepository.findBySessionId("x")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.getRecording("x"))
                    .isInstanceOf(SessionNotFoundException.class);
        }
    }
}
