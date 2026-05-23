package com.medconnect.teleconsulation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.medconnect.teleconsulation.dto.request.CreateSessionRequest;
import com.medconnect.teleconsulation.dto.response.JoinLinkResponse;
import com.medconnect.teleconsulation.dto.response.SessionResponse;
import com.medconnect.teleconsulation.dto.response.SessionStatusResponse;
import com.medconnect.teleconsulation.exception.GlobalExceptionHandler;
import com.medconnect.teleconsulation.exception.SessionAlreadyEndedException;
import com.medconnect.teleconsulation.exception.SessionNotFoundException;
import com.medconnect.teleconsulation.model.SessionStatus;
import com.medconnect.teleconsulation.service.SessionManagementService;
import com.medconnect.teleconsulation.service.VideoSessionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class TeleconsultControllerTest {

    MockMvc mockMvc;

    @Mock VideoSessionService videoSessionService;
    @Mock SessionManagementService sessionManagementService;

    @InjectMocks TeleconsultController controller;

    ObjectMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setMessageConverters(new MappingJackson2HttpMessageConverter(mapper))
                .build();
    }

    private SessionResponse sampleResponse() {
        return SessionResponse.builder()
                .id("mongo-1")
                .sessionId("sess-001")
                .appointmentId("appt-001")
                .doctorId("doc-001")
                .patientId("pat-001")
                .status(SessionStatus.CREATED)
                .createdAt(LocalDateTime.now())
                .build();
    }

    // ─── POST /api/teleconsult/sessions ───────────────────────────────────────

    @Nested
    @DisplayName("POST /api/teleconsult/sessions")
    class CreateSession {

        @Test
        @DisplayName("201: valid request")
        void happyPath() throws Exception {
            when(videoSessionService.createSession(any())).thenReturn(sampleResponse());

            CreateSessionRequest req = new CreateSessionRequest();
            req.setAppointmentId("appt-001");
            req.setDoctorId("doc-001");
            req.setPatientId("pat-001");

            mockMvc.perform(post("/api/teleconsult/sessions")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(req)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.sessionId").value("sess-001"))
                    .andExpect(jsonPath("$.status").value("CREATED"));
        }

        @Test
        @DisplayName("400: missing appointmentId")
        void missingAppointmentId() throws Exception {
            CreateSessionRequest req = new CreateSessionRequest();
            req.setDoctorId("doc-001");
            req.setPatientId("pat-001");

            mockMvc.perform(post("/api/teleconsult/sessions")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(req)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").exists());
        }

        @Test
        @DisplayName("400: empty body")
        void emptyBody() throws Exception {
            mockMvc.perform(post("/api/teleconsult/sessions")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("400: missing doctorId and patientId")
        void missingMultipleFields() throws Exception {
            CreateSessionRequest req = new CreateSessionRequest();
            req.setAppointmentId("appt-001");

            mockMvc.perform(post("/api/teleconsult/sessions")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(req)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("400: malformed JSON")
        void malformedJson() throws Exception {
            mockMvc.perform(post("/api/teleconsult/sessions")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{bad json}"))
                    .andExpect(status().isBadRequest());
        }
    }

    // ─── GET /api/teleconsult/sessions/{id} ───────────────────────────────────

    @Nested
    @DisplayName("GET /api/teleconsult/sessions/{id}")
    class GetSession {

        @Test
        @DisplayName("200: session found")
        void happyPath() throws Exception {
            when(videoSessionService.getSession("sess-001")).thenReturn(sampleResponse());

            mockMvc.perform(get("/api/teleconsult/sessions/sess-001"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.sessionId").value("sess-001"));
        }

        @Test
        @DisplayName("404: session not found")
        void notFound() throws Exception {
            when(videoSessionService.getSession("missing"))
                    .thenThrow(new SessionNotFoundException("Session not found: missing"));

            mockMvc.perform(get("/api/teleconsult/sessions/missing"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404));
        }
    }

    // ─── POST /api/teleconsult/sessions/{id}/start ────────────────────────────

    @Nested
    @DisplayName("POST /api/teleconsult/sessions/{id}/start")
    class StartSession {

        @Test
        @DisplayName("200: session starts successfully")
        void happyPath() throws Exception {
            SessionResponse active = sampleResponse();
            active.setStatus(SessionStatus.ACTIVE);
            when(videoSessionService.startSession("sess-001")).thenReturn(active);

            mockMvc.perform(post("/api/teleconsult/sessions/sess-001/start"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("ACTIVE"));
        }

        @Test
        @DisplayName("409: already active session")
        void alreadyActive() throws Exception {
            when(videoSessionService.startSession("sess-002"))
                    .thenThrow(new IllegalStateException("Session is already active: sess-002"));

            mockMvc.perform(post("/api/teleconsult/sessions/sess-002/start"))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("already active")));
        }

        @Test
        @DisplayName("409: session already ended")
        void alreadyEnded() throws Exception {
            when(videoSessionService.startSession("sess-003"))
                    .thenThrow(new SessionAlreadyEndedException("Session already ended: sess-003"));

            mockMvc.perform(post("/api/teleconsult/sessions/sess-003/start"))
                    .andExpect(status().isConflict());
        }

        @Test
        @DisplayName("404: session not found")
        void notFound() throws Exception {
            when(videoSessionService.startSession("x"))
                    .thenThrow(new SessionNotFoundException("Session not found: x"));

            mockMvc.perform(post("/api/teleconsult/sessions/x/start"))
                    .andExpect(status().isNotFound());
        }
    }

    // ─── GET /api/teleconsult/sessions/{id}/join ──────────────────────────────

    @Nested
    @DisplayName("GET /api/teleconsult/sessions/{id}/join")
    class JoinSession {

        @Test
        @DisplayName("200: valid DOCTOR role")
        void validDoctor() throws Exception {
            JoinLinkResponse linkResp = JoinLinkResponse.builder()
                    .sessionId("sess-001").role("DOCTOR")
                    .joinLink("/api/teleconsult/sessions/sess-001/join?token=abc&role=DOCTOR")
                    .expiresAt(LocalDateTime.now().plusHours(2)).build();
            when(videoSessionService.generateJoinLink("sess-001", "DOCTOR")).thenReturn(linkResp);

            mockMvc.perform(get("/api/teleconsult/sessions/sess-001/join").param("role", "DOCTOR"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.role").value("DOCTOR"))
                    .andExpect(jsonPath("$.joinLink").exists());
        }

        @Test
        @DisplayName("400: missing role param")
        void missingRole() throws Exception {
            mockMvc.perform(get("/api/teleconsult/sessions/sess-001/join"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("400: invalid role value")
        void invalidRole() throws Exception {
            when(videoSessionService.generateJoinLink("sess-001", "ADMIN"))
                    .thenThrow(new IllegalArgumentException("Invalid role 'ADMIN'. Allowed values: DOCTOR, PATIENT"));

            mockMvc.perform(get("/api/teleconsult/sessions/sess-001/join").param("role", "ADMIN"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Invalid role")));
        }
    }

    // ─── POST /api/teleconsult/sessions/{id}/end ──────────────────────────────

    @Nested
    @DisplayName("POST /api/teleconsult/sessions/{id}/end")
    class EndSession {

        @Test
        @DisplayName("200: session ended successfully")
        void happyPath() throws Exception {
            SessionResponse ended = sampleResponse();
            ended.setStatus(SessionStatus.ENDED);
            when(sessionManagementService.endSession("sess-001")).thenReturn(ended);

            mockMvc.perform(post("/api/teleconsult/sessions/sess-001/end"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("ENDED"));
        }

        @Test
        @DisplayName("409: already ended")
        void alreadyEnded() throws Exception {
            when(sessionManagementService.endSession("sess-001"))
                    .thenThrow(new SessionAlreadyEndedException("Session already ended: sess-001"));

            mockMvc.perform(post("/api/teleconsult/sessions/sess-001/end"))
                    .andExpect(status().isConflict());
        }

        @Test
        @DisplayName("404: session not found")
        void notFound() throws Exception {
            when(sessionManagementService.endSession("x"))
                    .thenThrow(new SessionNotFoundException("Session not found: x"));

            mockMvc.perform(post("/api/teleconsult/sessions/x/end"))
                    .andExpect(status().isNotFound());
        }
    }

    // ─── GET /api/teleconsult/sessions/{id}/status ────────────────────────────

    @Nested
    @DisplayName("GET /api/teleconsult/sessions/{id}/status")
    class GetStatus {

        @Test
        @DisplayName("200: returns status and participant count")
        void happyPath() throws Exception {
            SessionStatusResponse statusResp = SessionStatusResponse.builder()
                    .sessionId("sess-001").status(SessionStatus.ACTIVE)
                    .participantCount(2L).durationSeconds(120L).build();
            when(videoSessionService.getSessionStatus("sess-001")).thenReturn(statusResp);

            mockMvc.perform(get("/api/teleconsult/sessions/sess-001/status"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("ACTIVE"))
                    .andExpect(jsonPath("$.participantCount").value(2))
                    .andExpect(jsonPath("$.durationSeconds").value(120));
        }

        @Test
        @DisplayName("404: session not found")
        void notFound() throws Exception {
            when(videoSessionService.getSessionStatus("x"))
                    .thenThrow(new SessionNotFoundException("not found"));

            mockMvc.perform(get("/api/teleconsult/sessions/x/status"))
                    .andExpect(status().isNotFound());
        }
    }

    // ─── GET /api/teleconsult/sessions/{id}/summary ───────────────────────────

    @Nested
    @DisplayName("GET /api/teleconsult/sessions/{id}/summary")
    class GetSummary {

        @Test
        @DisplayName("200: returns summary map")
        void happyPath() throws Exception {
            when(sessionManagementService.generateSessionSummary("sess-001"))
                    .thenReturn(Map.of("sessionId", "sess-001", "status", "ENDED", "durationSeconds", 300L));

            mockMvc.perform(get("/api/teleconsult/sessions/sess-001/summary"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.sessionId").value("sess-001"))
                    .andExpect(jsonPath("$.status").value("ENDED"));
        }

        @Test
        @DisplayName("404: session not found")
        void notFound() throws Exception {
            when(sessionManagementService.generateSessionSummary("x"))
                    .thenThrow(new SessionNotFoundException("not found"));

            mockMvc.perform(get("/api/teleconsult/sessions/x/summary"))
                    .andExpect(status().isNotFound());
        }
    }
}
