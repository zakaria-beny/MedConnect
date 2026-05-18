package com.medconnect.teleconsulation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.medconnect.teleconsulation.dto.response.SessionResponse;
import com.medconnect.teleconsulation.exception.GlobalExceptionHandler;
import com.medconnect.teleconsulation.exception.SessionNotFoundException;
import com.medconnect.teleconsulation.model.SessionStatus;
import com.medconnect.teleconsulation.service.ScreenShareService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ScreenShareControllerTest {

    MockMvc mockMvc;

    @Mock ScreenShareService screenShareService;
    @InjectMocks ScreenShareController controller;

    @BeforeEach
    void setUp() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setMessageConverters(
                        new StringHttpMessageConverter(),
                        new MappingJackson2HttpMessageConverter(mapper))
                .build();
    }

    private SessionResponse sharingResponse() {
        return SessionResponse.builder()
                .sessionId("sess-001")
                .status(SessionStatus.ACTIVE)
                .screenSharing(true)
                .build();
    }

    private SessionResponse notSharingResponse() {
        return SessionResponse.builder()
                .sessionId("sess-001")
                .status(SessionStatus.ACTIVE)
                .screenSharing(false)
                .build();
    }

    // ─── POST /sessions/{id}/share-screen/start ───────────────────────────────

    @Nested
    @DisplayName("POST /sessions/{id}/share-screen/start")
    class StartScreenShare {

        @Test
        @DisplayName("200: doctor starts screen share successfully")
        void happyPath() throws Exception {
            when(screenShareService.startScreenShare("sess-001", "doc-001")).thenReturn(sharingResponse());

            mockMvc.perform(post("/api/teleconsult/sessions/sess-001/share-screen/start")
                            .param("doctorId", "doc-001"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.screenSharing").value(true));
        }

        @Test
        @DisplayName("400: missing doctorId param")
        void missingDoctorId() throws Exception {
            mockMvc.perform(post("/api/teleconsult/sessions/sess-001/share-screen/start"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("409: wrong doctor (not session owner)")
        void wrongDoctor() throws Exception {
            when(screenShareService.startScreenShare("sess-001", "doc-999"))
                    .thenThrow(new IllegalStateException("Only the doctor can start screen sharing"));

            mockMvc.perform(post("/api/teleconsult/sessions/sess-001/share-screen/start")
                            .param("doctorId", "doc-999"))
                    .andExpect(status().isConflict());
        }

        @Test
        @DisplayName("409: screen sharing already active")
        void alreadySharing() throws Exception {
            when(screenShareService.startScreenShare("sess-001", "doc-001"))
                    .thenThrow(new IllegalStateException("Screen sharing is already active"));

            mockMvc.perform(post("/api/teleconsult/sessions/sess-001/share-screen/start")
                            .param("doctorId", "doc-001"))
                    .andExpect(status().isConflict());
        }

        @Test
        @DisplayName("404: session not found")
        void notFound() throws Exception {
            when(screenShareService.startScreenShare("x", "doc-001"))
                    .thenThrow(new SessionNotFoundException("Session not found: x"));

            mockMvc.perform(post("/api/teleconsult/sessions/x/share-screen/start")
                            .param("doctorId", "doc-001"))
                    .andExpect(status().isNotFound());
        }
    }

    // ─── POST /sessions/{id}/share-screen/stop ────────────────────────────────

    @Nested
    @DisplayName("POST /sessions/{id}/share-screen/stop")
    class StopScreenShare {

        @Test
        @DisplayName("200: screen sharing stopped")
        void happyPath() throws Exception {
            when(screenShareService.stopScreenShare("sess-001")).thenReturn(notSharingResponse());

            mockMvc.perform(post("/api/teleconsult/sessions/sess-001/share-screen/stop"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.screenSharing").value(false));
        }

        @Test
        @DisplayName("404: session not found")
        void notFound() throws Exception {
            when(screenShareService.stopScreenShare("x"))
                    .thenThrow(new SessionNotFoundException("Session not found: x"));

            mockMvc.perform(post("/api/teleconsult/sessions/x/share-screen/stop"))
                    .andExpect(status().isNotFound());
        }
    }

    // ─── POST /sessions/{id}/share-screen/image ───────────────────────────────

    @Nested
    @DisplayName("POST /sessions/{id}/share-screen/image")
    class ShareImage {

        @Test
        @DisplayName("200: valid image data shared")
        void happyPath() throws Exception {
            when(screenShareService.shareImage(eq("sess-001"), anyString()))
                    .thenReturn(Map.of("status", "shared", "sessionId", "sess-001", "imageSize", 1000));

            mockMvc.perform(post("/api/teleconsult/sessions/sess-001/share-screen/image")
                            .contentType(MediaType.TEXT_PLAIN)
                            .content("base64encodedimagedata"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("shared"));
        }

        @Test
        @DisplayName("400: null/blank image data")
        void blankImage() throws Exception {
            when(screenShareService.shareImage(eq("sess-001"), anyString()))
                    .thenThrow(new IllegalArgumentException("Image data cannot be null or empty"));

            mockMvc.perform(post("/api/teleconsult/sessions/sess-001/share-screen/image")
                            .contentType(MediaType.TEXT_PLAIN)
                            .content("   "))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("404: session not found")
        void notFound() throws Exception {
            when(screenShareService.shareImage(eq("x"), anyString()))
                    .thenThrow(new SessionNotFoundException("Session not found: x"));

            mockMvc.perform(post("/api/teleconsult/sessions/x/share-screen/image")
                            .contentType(MediaType.TEXT_PLAIN)
                            .content("imagedata"))
                    .andExpect(status().isNotFound());
        }
    }
}
