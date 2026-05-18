package com.medconnect.teleconsulation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.medconnect.teleconsulation.dto.response.RecordingResponse;
import com.medconnect.teleconsulation.exception.ConsentRequiredException;
import com.medconnect.teleconsulation.exception.GlobalExceptionHandler;
import com.medconnect.teleconsulation.exception.SessionNotFoundException;
import com.medconnect.teleconsulation.service.RecordingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class RecordingControllerTest {

    MockMvc mockMvc;

    @Mock RecordingService recordingService;
    @InjectMocks RecordingController controller;

    @BeforeEach
    void setUp() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setMessageConverters(new MappingJackson2HttpMessageConverter(mapper))
                .build();
    }

    private RecordingResponse activeRecording() {
        return RecordingResponse.builder()
                .id("rec-001")
                .sessionId("sess-001")
                .consentGiven(true)
                .startedAt(LocalDateTime.now())
                .deleted(false)
                .build();
    }

    private RecordingResponse stoppedRecording() {
        return RecordingResponse.builder()
                .id("rec-001")
                .sessionId("sess-001")
                .consentGiven(true)
                .startedAt(LocalDateTime.now().minusMinutes(10))
                .stoppedAt(LocalDateTime.now())
                .durationSeconds(600L)
                .deleted(false)
                .build();
    }

    // ─── POST /sessions/{id}/record-start ─────────────────────────────────────

    @Nested
    @DisplayName("POST /sessions/{id}/record-start")
    class StartRecording {

        @Test
        @DisplayName("201: recording starts successfully")
        void happyPath() throws Exception {
            when(recordingService.startRecording("sess-001")).thenReturn(activeRecording());

            mockMvc.perform(post("/api/teleconsult/sessions/sess-001/record-start"))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.consentGiven").value(true))
                    .andExpect(jsonPath("$.stoppedAt").doesNotExist());
        }

        @Test
        @DisplayName("403: consent not given")
        void noConsent() throws Exception {
            when(recordingService.startRecording("sess-001"))
                    .thenThrow(new ConsentRequiredException("Patient consent is required"));

            mockMvc.perform(post("/api/teleconsult/sessions/sess-001/record-start"))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("consent")));
        }

        @Test
        @DisplayName("409: recording already in progress")
        void alreadyRecording() throws Exception {
            when(recordingService.startRecording("sess-001"))
                    .thenThrow(new IllegalStateException("Recording is already in progress"));

            mockMvc.perform(post("/api/teleconsult/sessions/sess-001/record-start"))
                    .andExpect(status().isConflict());
        }

        @Test
        @DisplayName("404: session not found")
        void sessionNotFound() throws Exception {
            when(recordingService.startRecording("x"))
                    .thenThrow(new SessionNotFoundException("Session not found: x"));

            mockMvc.perform(post("/api/teleconsult/sessions/x/record-start"))
                    .andExpect(status().isNotFound());
        }
    }

    // ─── POST /sessions/{id}/record-stop ──────────────────────────────────────

    @Nested
    @DisplayName("POST /sessions/{id}/record-stop")
    class StopRecording {

        @Test
        @DisplayName("200: recording stopped, duration stored")
        void happyPath() throws Exception {
            when(recordingService.stopRecording("sess-001")).thenReturn(stoppedRecording());

            mockMvc.perform(post("/api/teleconsult/sessions/sess-001/record-stop"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.stoppedAt").exists())
                    .andExpect(jsonPath("$.durationSeconds").value(600));
        }

        @Test
        @DisplayName("409: recording already stopped (double-stop guard)")
        void doubleStop() throws Exception {
            when(recordingService.stopRecording("sess-001"))
                    .thenThrow(new IllegalStateException("Recording has already been stopped"));

            mockMvc.perform(post("/api/teleconsult/sessions/sess-001/record-stop"))
                    .andExpect(status().isConflict());
        }

        @Test
        @DisplayName("404: no recording found for session")
        void noRecording() throws Exception {
            when(recordingService.stopRecording("sess-001"))
                    .thenThrow(new SessionNotFoundException("No recording found for session: sess-001"));

            mockMvc.perform(post("/api/teleconsult/sessions/sess-001/record-stop"))
                    .andExpect(status().isNotFound());
        }
    }

    // ─── GET /sessions/{id}/recording ─────────────────────────────────────────

    @Nested
    @DisplayName("GET /sessions/{id}/recording")
    class GetRecording {

        @Test
        @DisplayName("200: returns recording metadata")
        void happyPath() throws Exception {
            when(recordingService.getRecording("sess-001")).thenReturn(stoppedRecording());

            mockMvc.perform(get("/api/teleconsult/sessions/sess-001/recording"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.sessionId").value("sess-001"))
                    .andExpect(jsonPath("$.durationSeconds").value(600));
        }

        @Test
        @DisplayName("404: no recording found (not 409)")
        void notFound() throws Exception {
            when(recordingService.getRecording("x"))
                    .thenThrow(new SessionNotFoundException("No recording found for session: x"));

            mockMvc.perform(get("/api/teleconsult/sessions/x/recording"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404));
        }
    }
}
