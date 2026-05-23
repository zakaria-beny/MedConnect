package com.medconnect.teleconsulation.controller;

import com.medconnect.teleconsulation.dto.response.SessionResponse;
import com.medconnect.teleconsulation.service.ScreenShareService;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Screen sharing: doctor-only start/stop, medical image sharing.
 */
@RestController
@RequestMapping("/api/teleconsult/sessions/{id}/share-screen")
@RequiredArgsConstructor
@Validated
public class ScreenShareController {

    private final ScreenShareService screenShareService;

    @PostMapping("/start")
    public ResponseEntity<SessionResponse> startScreenShare(
            @PathVariable String id,
            @RequestParam @NotBlank(message = "doctorId is required") String doctorId) {
        return ResponseEntity.ok(screenShareService.startScreenShare(id, doctorId));
    }

    @PostMapping("/stop")
    public ResponseEntity<SessionResponse> stopScreenShare(@PathVariable String id) {
        return ResponseEntity.ok(screenShareService.stopScreenShare(id));
    }

    @PostMapping("/image")
    public ResponseEntity<Map<String, Object>> shareImage(
            @PathVariable String id,
            @RequestBody String imageData) {
        return ResponseEntity.ok(screenShareService.shareImage(id, imageData));
    }
}
