package com.medconnect.teleconsulation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.medconnect.teleconsulation.dto.request.SendMessageRequest;
import com.medconnect.teleconsulation.dto.response.ChatResponse;
import com.medconnect.teleconsulation.exception.GlobalExceptionHandler;
import com.medconnect.teleconsulation.exception.SessionNotFoundException;
import com.medconnect.teleconsulation.model.ChatMessage;
import com.medconnect.teleconsulation.service.ChatService;
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
class ChatControllerTest {

    MockMvc mockMvc;
    ObjectMapper mapper;

    @Mock ChatService chatService;
    @InjectMocks ChatController controller;

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

    private ChatResponse chatWithMessages() {
        ChatMessage msg = ChatMessage.builder()
                .senderId("doc-001")
                .message("Hello patient")
                .sentAt(LocalDateTime.now())
                .build();
        return ChatResponse.builder()
                .sessionId("sess-001")
                .messages(List.of(msg))
                .lastUpdated(LocalDateTime.now())
                .build();
    }

    // ─── GET /sessions/{id}/chat ──────────────────────────────────────────────

    @Nested
    @DisplayName("GET /sessions/{id}/chat")
    class GetChat {

        @Test
        @DisplayName("200: returns chat history")
        void happyPath() throws Exception {
            when(chatService.getChat("sess-001")).thenReturn(chatWithMessages());

            mockMvc.perform(get("/api/teleconsult/sessions/sess-001/chat"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.sessionId").value("sess-001"))
                    .andExpect(jsonPath("$.messages.length()").value(1))
                    .andExpect(jsonPath("$.messages[0].senderId").value("doc-001"));
        }

        @Test
        @DisplayName("200: empty session returns empty messages list")
        void emptyChat() throws Exception {
            when(chatService.getChat("sess-empty"))
                    .thenReturn(ChatResponse.builder().sessionId("sess-empty")
                            .messages(List.of()).build());

            mockMvc.perform(get("/api/teleconsult/sessions/sess-empty/chat"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.messages").isEmpty());
        }

        @Test
        @DisplayName("404: session not found")
        void notFound() throws Exception {
            when(chatService.getChat("x"))
                    .thenThrow(new SessionNotFoundException("not found"));

            mockMvc.perform(get("/api/teleconsult/sessions/x/chat"))
                    .andExpect(status().isNotFound());
        }
    }

    // ─── POST /sessions/{id}/chat/message ────────────────────────────────────

    @Nested
    @DisplayName("POST /sessions/{id}/chat/message")
    class SendMessage {

        @Test
        @DisplayName("201: message sent successfully")
        void happyPath() throws Exception {
            when(chatService.sendMessage(eq("sess-001"), eq("doc-001"), eq("Hello patient")))
                    .thenReturn(chatWithMessages());

            SendMessageRequest req = new SendMessageRequest();
            req.setSenderId("doc-001");
            req.setMessage("Hello patient");

            mockMvc.perform(post("/api/teleconsult/sessions/sess-001/chat/message")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(req)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.messages[0].message").value("Hello patient"));
        }

        @Test
        @DisplayName("400: missing senderId")
        void missingSenderId() throws Exception {
            SendMessageRequest req = new SendMessageRequest();
            req.setMessage("Hello");

            mockMvc.perform(post("/api/teleconsult/sessions/sess-001/chat/message")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(req)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("400: missing message text")
        void missingMessage() throws Exception {
            SendMessageRequest req = new SendMessageRequest();
            req.setSenderId("doc-001");

            mockMvc.perform(post("/api/teleconsult/sessions/sess-001/chat/message")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(req)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("400: empty body")
        void emptyBody() throws Exception {
            mockMvc.perform(post("/api/teleconsult/sessions/sess-001/chat/message")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("400: malformed JSON")
        void malformedJson() throws Exception {
            mockMvc.perform(post("/api/teleconsult/sessions/sess-001/chat/message")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{invalid}"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Edge case: XSS payload in message → accepted (sanitisation is frontend concern)")
        void xssPayload() throws Exception {
            String xssMsg = "<script>alert('xss')</script>";
            SendMessageRequest req = new SendMessageRequest();
            req.setSenderId("doc-001");
            req.setMessage(xssMsg);

            ChatResponse resp = ChatResponse.builder()
                    .sessionId("sess-001")
                    .messages(List.of(ChatMessage.builder()
                            .senderId("doc-001").message(xssMsg)
                            .sentAt(LocalDateTime.now()).build()))
                    .build();
            when(chatService.sendMessage(any(), any(), any())).thenReturn(resp);

            mockMvc.perform(post("/api/teleconsult/sessions/sess-001/chat/message")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(req)))
                    .andExpect(status().isCreated());
        }

        @Test
        @DisplayName("404: session not found")
        void notFound() throws Exception {
            when(chatService.sendMessage(any(), any(), any()))
                    .thenThrow(new SessionNotFoundException("not found"));

            SendMessageRequest req = new SendMessageRequest();
            req.setSenderId("doc-001");
            req.setMessage("hi");

            mockMvc.perform(post("/api/teleconsult/sessions/x/chat/message")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(req)))
                    .andExpect(status().isNotFound());
        }
    }
}
