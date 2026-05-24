package com.medconnect.teleconsulation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.medconnect.teleconsulation.dto.request.AdmitPatientRequest;
import com.medconnect.teleconsulation.dto.response.WaitingRoomResponse;
import com.medconnect.teleconsulation.exception.GlobalExceptionHandler;
import com.medconnect.teleconsulation.exception.SessionNotFoundException;
import com.medconnect.teleconsulation.service.WaitingRoomService;
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
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class WaitingRoomControllerTest {

    MockMvc mockMvc;
    ObjectMapper mapper;

    @Mock WaitingRoomService waitingRoomService;
    @InjectMocks WaitingRoomController controller;

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

    private WaitingRoomResponse sampleEntry(String sessionId, String patientId, int pos) {
        return WaitingRoomResponse.builder()
                .sessionId(sessionId)
                .patientId(patientId)
                .position(pos)
                .estimatedWaitMinutes((pos - 1) * 15)
                .joinedAt(LocalDateTime.now())
                .admitted(false)
                .build();
    }

    // ─── POST /api/teleconsult/sessions/{id}/waiting-room ─────────────────────

    @Nested
    @DisplayName("POST /sessions/{id}/waiting-room")
    class JoinWaitingRoom {

        @Test
        @DisplayName("201: patient joins successfully")
        void happyPath() throws Exception {
            when(waitingRoomService.addToWaitingRoom("sess-001", "pat-001"))
                    .thenReturn(sampleEntry("sess-001", "pat-001", 1));

            mockMvc.perform(post("/api/teleconsult/sessions/sess-001/waiting-room")
                            .param("patientId", "pat-001"))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.position").value(1))
                    .andExpect(jsonPath("$.admitted").value(false));
        }

        @Test
        @DisplayName("400: missing patientId param")
        void missingPatientId() throws Exception {
            mockMvc.perform(post("/api/teleconsult/sessions/sess-001/waiting-room"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("404: session not found")
        void sessionNotFound() throws Exception {
            when(waitingRoomService.addToWaitingRoom("x", "pat-001"))
                    .thenThrow(new SessionNotFoundException("Session not found: x"));

            mockMvc.perform(post("/api/teleconsult/sessions/x/waiting-room")
                            .param("patientId", "pat-001"))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("409: patient already in waiting room")
        void duplicate() throws Exception {
            when(waitingRoomService.addToWaitingRoom("sess-001", "pat-001"))
                    .thenThrow(new IllegalStateException("already in the waiting room"));

            mockMvc.perform(post("/api/teleconsult/sessions/sess-001/waiting-room")
                            .param("patientId", "pat-001"))
                    .andExpect(status().isConflict());
        }

        @Test
        @DisplayName("409: session already ended")
        void endedSession() throws Exception {
            when(waitingRoomService.addToWaitingRoom("sess-ended", "pat-001"))
                    .thenThrow(new IllegalStateException("Cannot join waiting room: session has already ended"));

            mockMvc.perform(post("/api/teleconsult/sessions/sess-ended/waiting-room")
                            .param("patientId", "pat-001"))
                    .andExpect(status().isConflict());
        }
    }

    // ─── GET /api/teleconsult/sessions/{id}/queue-position ───────────────────

    @Nested
    @DisplayName("GET /sessions/{id}/queue-position")
    class GetQueuePosition {

        @Test
        @DisplayName("200: returns patient queue position")
        void happyPath() throws Exception {
            when(waitingRoomService.getQueuePosition("pat-001"))
                    .thenReturn(sampleEntry("sess-001", "pat-001", 2));

            mockMvc.perform(get("/api/teleconsult/sessions/sess-001/queue-position")
                            .param("patientId", "pat-001"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.position").value(2));
        }

        @Test
        @DisplayName("400: missing patientId")
        void missingPatient() throws Exception {
            mockMvc.perform(get("/api/teleconsult/sessions/sess-001/queue-position"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("404: patient not in any waiting room")
        void notInQueue() throws Exception {
            when(waitingRoomService.getQueuePosition("pat-999"))
                    .thenThrow(new SessionNotFoundException("Patient pat-999 is not in any waiting room"));

            mockMvc.perform(get("/api/teleconsult/sessions/sess-001/queue-position")
                            .param("patientId", "pat-999"))
                    .andExpect(status().isNotFound());
        }
    }

    // ─── POST /api/teleconsult/sessions/{id}/admit-next ───────────────────────

    @Nested
    @DisplayName("POST /sessions/{id}/admit-next")
    class AdmitNext {

        @Test
        @DisplayName("200: admit next patient with no body")
        void admitNextNoBody() throws Exception {
            WaitingRoomResponse admitted = sampleEntry("sess-001", "pat-001", 1);
            admitted.setAdmitted(true);
            when(waitingRoomService.admitFromWaitingRoom("sess-001", null)).thenReturn(admitted);

            mockMvc.perform(post("/api/teleconsult/sessions/sess-001/admit-next"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.admitted").value(true));
        }

        @Test
        @DisplayName("200: admit specific patient by patientId in body")
        void admitSpecific() throws Exception {
            WaitingRoomResponse admitted = sampleEntry("sess-001", "pat-002", 2);
            admitted.setAdmitted(true);
            when(waitingRoomService.admitFromWaitingRoom("sess-001", "pat-002")).thenReturn(admitted);

            AdmitPatientRequest req = new AdmitPatientRequest();
            req.setPatientId("pat-002");

            mockMvc.perform(post("/api/teleconsult/sessions/sess-001/admit-next")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(req)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.patientId").value("pat-002"));
        }

        @Test
        @DisplayName("409: no patients in queue")
        void emptyQueue() throws Exception {
            when(waitingRoomService.admitFromWaitingRoom("sess-001", null))
                    .thenThrow(new IllegalStateException("No patients waiting for session: sess-001"));

            mockMvc.perform(post("/api/teleconsult/sessions/sess-001/admit-next"))
                    .andExpect(status().isConflict());
        }
    }

    // ─── GET /api/teleconsult/wait-queue/{doctorId} ───────────────────────────

    @Nested
    @DisplayName("GET /wait-queue/{doctorId}")
    class GetWaitQueue {

        @Test
        @DisplayName("200: returns full queue for doctor")
        void happyPath() throws Exception {
            WaitingRoomResponse queue = WaitingRoomResponse.builder()
                    .estimatedWaitMinutes(30)
                    .queue(List.of(
                            WaitingRoomResponse.QueueEntry.builder()
                                    .patientId("pat-001").position(1)
                                    .joinedAt(LocalDateTime.now()).build(),
                            WaitingRoomResponse.QueueEntry.builder()
                                    .patientId("pat-002").position(2)
                                    .joinedAt(LocalDateTime.now()).build()
                    ))
                    .build();
            when(waitingRoomService.getWaitQueue("doc-001")).thenReturn(queue);

            mockMvc.perform(get("/api/teleconsult/wait-queue/doc-001"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.queue.length()").value(2))
                    .andExpect(jsonPath("$.estimatedWaitMinutes").value(30));
        }

        @Test
        @DisplayName("200: empty queue returns empty list")
        void emptyQueue() throws Exception {
            when(waitingRoomService.getWaitQueue("doc-999"))
                    .thenReturn(WaitingRoomResponse.builder().queue(List.of()).estimatedWaitMinutes(0).build());

            mockMvc.perform(get("/api/teleconsult/wait-queue/doc-999"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.queue").isEmpty());
        }
    }
}
