package com.medconnect.teleconsulation.controller;

import com.medconnect.teleconsulation.dto.request.CreateSessionRequest;
import com.medconnect.teleconsulation.dto.response.JoinLinkResponse;
import com.medconnect.teleconsulation.dto.response.SessionResponse;
import com.medconnect.teleconsulation.dto.response.SessionStatusResponse;
import com.medconnect.teleconsulation.service.SessionManagementService;
import com.medconnect.teleconsulation.service.VideoSessionService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Session lifecycle: create, get, start, join, end, status, summary.
 */
@RestController
@RequestMapping("/api/teleconsult/sessions")
@RequiredArgsConstructor
@Validated
public class TeleconsultController {

    private final VideoSessionService videoSessionService;
    private final SessionManagementService sessionManagementService;

    @PostMapping
    public ResponseEntity<SessionResponse> createSession(@Valid @RequestBody CreateSessionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(videoSessionService.createSession(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SessionResponse> getSession(@PathVariable String id) {
        return ResponseEntity.ok(videoSessionService.getSession(id));
    }

    @PostMapping("/{id}/start")
    public ResponseEntity<SessionResponse> startSession(@PathVariable String id) {
        return ResponseEntity.ok(videoSessionService.startSession(id));
    }

    @GetMapping("/{id}/join")
    public ResponseEntity<JoinLinkResponse> joinSession(
            @PathVariable String id,
            @RequestParam @NotBlank(message = "role is required") String role) {
        return ResponseEntity.ok(videoSessionService.generateJoinLink(id, role));
    }

    @PostMapping("/{id}/end")
    public ResponseEntity<SessionResponse> endSession(@PathVariable String id) {
        return ResponseEntity.ok(sessionManagementService.endSession(id));
    }

    @GetMapping("/{id}/status")
    public ResponseEntity<SessionStatusResponse> getStatus(@PathVariable String id) {
        return ResponseEntity.ok(videoSessionService.getSessionStatus(id));
    }

    @GetMapping("/{id}/summary")
    public ResponseEntity<Map<String, Object>> getSessionSummary(@PathVariable String id) {
        return ResponseEntity.ok(sessionManagementService.generateSessionSummary(id));
    }
}
