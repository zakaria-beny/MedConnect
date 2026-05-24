package com.medconnect.teleconsulation.service;

import com.medconnect.teleconsulation.dto.response.SessionResponse;
import com.medconnect.teleconsulation.exception.SessionNotFoundException;
import com.medconnect.teleconsulation.model.SessionStatus;
import com.medconnect.teleconsulation.model.VideoSession;
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

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ScreenShareServiceTest {

    @Mock VideoSessionRepository videoSessionRepository;
    @Mock SessionEventRepository sessionEventRepository;
    @Mock VideoSessionService videoSessionService;

    @InjectMocks ScreenShareService service;

    private VideoSession session;

    @BeforeEach
    void setUp() {
        session = VideoSession.builder()
                .sessionId("sess-001")
                .doctorId("doc-001")
                .status(SessionStatus.ACTIVE)
                .screenSharing(false)
                .build();
    }

    // ─── startScreenShare ─────────────────────────────────────────────────────

    @Nested
    @DisplayName("startScreenShare")
    class StartScreenShare {

        @Test
        @DisplayName("Happy path: doctor starts screen share")
        void happyPath() {
            when(videoSessionRepository.findBySessionId("sess-001")).thenReturn(Optional.of(session));
            when(videoSessionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
            when(sessionEventRepository.save(any())).thenReturn(null);
            SessionResponse mockResp = SessionResponse.builder().sessionId("sess-001").screenSharing(true).build();
            when(videoSessionService.toResponse(any())).thenReturn(mockResp);

            SessionResponse resp = service.startScreenShare("sess-001", "doc-001");

            assertThat(resp.isScreenSharing()).isTrue();
        }

        @Test
        @DisplayName("Error: wrong doctor → 409")
        void wrongDoctor() {
            when(videoSessionRepository.findBySessionId("sess-001")).thenReturn(Optional.of(session));

            assertThatThrownBy(() -> service.startScreenShare("sess-001", "doc-999"))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Only the doctor");
        }

        @Test
        @DisplayName("Error: screen sharing already active → 409")
        void alreadySharing() {
            session.setScreenSharing(true);
            when(videoSessionRepository.findBySessionId("sess-001")).thenReturn(Optional.of(session));

            assertThatThrownBy(() -> service.startScreenShare("sess-001", "doc-001"))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("already active");
        }

        @Test
        @DisplayName("Error: session not found → 404")
        void notFound() {
            when(videoSessionRepository.findBySessionId("x")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.startScreenShare("x", "doc-001"))
                    .isInstanceOf(SessionNotFoundException.class);
        }
    }

    // ─── shareImage ───────────────────────────────────────────────────────────

    @Nested
    @DisplayName("shareImage")
    class ShareImage {

        @Test
        @DisplayName("Happy path: valid image data")
        void happyPath() {
            when(videoSessionRepository.findBySessionId("sess-001")).thenReturn(Optional.of(session));
            when(sessionEventRepository.save(any())).thenReturn(null);

            Map<String, Object> resp = service.shareImage("sess-001", "base64encodedimage");

            assertThat(resp.get("status")).isEqualTo("shared");
            assertThat(resp.get("sessionId")).isEqualTo("sess-001");
        }

        @Test
        @DisplayName("Edge case: null imageData → 400")
        void nullImage() {
            assertThatThrownBy(() -> service.shareImage("sess-001", null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("null or empty");
        }

        @Test
        @DisplayName("Edge case: blank imageData → 400")
        void blankImage() {
            assertThatThrownBy(() -> service.shareImage("sess-001", "   "))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }
}
