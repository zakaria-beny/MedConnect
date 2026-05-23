package com.medconnect.teleconsulation.service;

import com.medconnect.teleconsulation.dto.response.WaitingRoomResponse;
import com.medconnect.teleconsulation.exception.SessionNotFoundException;
import com.medconnect.teleconsulation.model.SessionStatus;
import com.medconnect.teleconsulation.model.VideoSession;
import com.medconnect.teleconsulation.model.WaitingRoom;
import com.medconnect.teleconsulation.repository.VideoSessionRepository;
import com.medconnect.teleconsulation.repository.WaitingRoomRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WaitingRoomServiceTest {

    @Mock WaitingRoomRepository waitingRoomRepository;
    @Mock VideoSessionRepository videoSessionRepository;

    @InjectMocks WaitingRoomService service;

    private VideoSession activeSession;
    private VideoSession endedSession;

    @BeforeEach
    void setUp() {
        activeSession = VideoSession.builder()
                .sessionId("sess-001")
                .doctorId("doc-001")
                .status(SessionStatus.ACTIVE)
                .build();

        endedSession = VideoSession.builder()
                .sessionId("sess-002")
                .doctorId("doc-001")
                .status(SessionStatus.ENDED)
                .build();
    }

    // ─── addToWaitingRoom ─────────────────────────────────────────────────────

    @Nested
    @DisplayName("addToWaitingRoom")
    class AddToWaitingRoom {

        @Test
        @DisplayName("Happy path: patient joins active session waiting room at position 1")
        void happyPath() {
            when(videoSessionRepository.findBySessionId("sess-001")).thenReturn(Optional.of(activeSession));
            when(waitingRoomRepository.findBySessionIdAndPatientId("sess-001", "pat-001"))
                    .thenReturn(Optional.empty());
            when(waitingRoomRepository.countBySessionIdAndAdmittedFalse("sess-001")).thenReturn(0L);
            when(waitingRoomRepository.save(any())).thenAnswer(inv -> {
                WaitingRoom w = inv.getArgument(0);
                w.setJoinedAt(LocalDateTime.now());
                return w;
            });

            WaitingRoomResponse resp = service.addToWaitingRoom("sess-001", "pat-001");

            assertThat(resp.getPosition()).isEqualTo(1);
            assertThat(resp.getEstimatedWaitMinutes()).isEqualTo(0);
            assertThat(resp.isAdmitted()).isFalse();
        }

        @Test
        @DisplayName("Happy path: second patient gets position 2, 15-min wait")
        void secondPatient() {
            when(videoSessionRepository.findBySessionId("sess-001")).thenReturn(Optional.of(activeSession));
            when(waitingRoomRepository.findBySessionIdAndPatientId("sess-001", "pat-002"))
                    .thenReturn(Optional.empty());
            when(waitingRoomRepository.countBySessionIdAndAdmittedFalse("sess-001")).thenReturn(1L);
            when(waitingRoomRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            WaitingRoomResponse resp = service.addToWaitingRoom("sess-001", "pat-002");

            assertThat(resp.getPosition()).isEqualTo(2);
            assertThat(resp.getEstimatedWaitMinutes()).isEqualTo(15);
        }

        @Test
        @DisplayName("Error: patient already in waiting room → 409")
        void duplicate() {
            when(videoSessionRepository.findBySessionId("sess-001")).thenReturn(Optional.of(activeSession));
            when(waitingRoomRepository.findBySessionIdAndPatientId("sess-001", "pat-001"))
                    .thenReturn(Optional.of(WaitingRoom.builder().build()));

            assertThatThrownBy(() -> service.addToWaitingRoom("sess-001", "pat-001"))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("already in the waiting room");
        }

        @Test
        @DisplayName("Error: session already ended → 409")
        void endedSession() {
            when(videoSessionRepository.findBySessionId("sess-002")).thenReturn(Optional.of(endedSession));

            assertThatThrownBy(() -> service.addToWaitingRoom("sess-002", "pat-001"))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("already ended");
        }

        @Test
        @DisplayName("Error: session not found → 404")
        void sessionNotFound() {
            when(videoSessionRepository.findBySessionId("x")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.addToWaitingRoom("x", "pat-001"))
                    .isInstanceOf(SessionNotFoundException.class);
        }
    }

    // ─── admitFromWaitingRoom ─────────────────────────────────────────────────

    @Nested
    @DisplayName("admitFromWaitingRoom")
    class AdmitFromWaitingRoom {

        @Test
        @DisplayName("Happy path: admit specific patient by patientId")
        void admitByPatientId() {
            WaitingRoom entry = WaitingRoom.builder()
                    .sessionId("sess-001").patientId("pat-001").position(1).admitted(false)
                    .joinedAt(LocalDateTime.now().minusMinutes(5)).build();
            when(waitingRoomRepository.findBySessionIdAndPatientId("sess-001", "pat-001"))
                    .thenReturn(Optional.of(entry));
            when(waitingRoomRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            WaitingRoomResponse resp = service.admitFromWaitingRoom("sess-001", "pat-001");

            assertThat(resp.isAdmitted()).isTrue();
            assertThat(resp.getEstimatedWaitMinutes()).isEqualTo(0);
        }

        @Test
        @DisplayName("Happy path: admit next (no patientId) → picks first in queue")
        void admitNext() {
            WaitingRoom entry = WaitingRoom.builder()
                    .sessionId("sess-001").patientId("pat-001").position(1).admitted(false)
                    .joinedAt(LocalDateTime.now().minusMinutes(2)).build();
            when(waitingRoomRepository.findBySessionIdAndAdmittedFalseOrderByPositionAsc("sess-001"))
                    .thenReturn(List.of(entry));
            when(waitingRoomRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            WaitingRoomResponse resp = service.admitFromWaitingRoom("sess-001", null);

            assertThat(resp.isAdmitted()).isTrue();
        }

        @Test
        @DisplayName("Error: empty queue → 409")
        void emptyQueue() {
            when(waitingRoomRepository.findBySessionIdAndAdmittedFalseOrderByPositionAsc("sess-001"))
                    .thenReturn(List.of());

            assertThatThrownBy(() -> service.admitFromWaitingRoom("sess-001", null))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("No patients waiting");
        }

        @Test
        @DisplayName("Error: patient not in queue → 404")
        void patientNotFound() {
            when(waitingRoomRepository.findBySessionIdAndPatientId("sess-001", "pat-999"))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.admitFromWaitingRoom("sess-001", "pat-999"))
                    .isInstanceOf(SessionNotFoundException.class);
        }
    }
}
