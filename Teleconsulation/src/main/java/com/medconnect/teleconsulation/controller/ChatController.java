package com.medconnect.teleconsulation.controller;

import com.medconnect.teleconsulation.dto.request.SendMessageRequest;
import com.medconnect.teleconsulation.dto.response.ChatResponse;
import com.medconnect.teleconsulation.service.ChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * In-session chat: send messages and retrieve full chat history.
 */
@RestController
@RequestMapping("/api/teleconsult/sessions/{id}/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @GetMapping
    public ResponseEntity<ChatResponse> getChat(@PathVariable String id) {
        return ResponseEntity.ok(chatService.getChat(id));
    }

    @PostMapping("/message")
    public ResponseEntity<ChatResponse> sendMessage(
            @PathVariable String id,
            @Valid @RequestBody SendMessageRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(chatService.sendMessage(id, request.getSenderId(), request.getMessage()));
    }
}
