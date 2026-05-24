package com.medconnect.teleconsulation.controller;

import com.medconnect.teleconsulation.dto.response.RecordingResponse;
import com.medconnect.teleconsulation.service.RecordingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Recording management: start (with consent), stop, retrieve metadata.
 * All recordings are AES-256 encrypted and expire after 2 years.
 */
@RestController
@RequestMapping("/api/teleconsult/sessions/{id}")
@RequiredArgsConstructor
public class RecordingController {

    private final RecordingService recordingService;

    @PostMapping("/record-start")
    public ResponseEntity<RecordingResponse> startRecording(@PathVariable String id) {
        return ResponseEntity.status(HttpStatus.CREATED).body(recordingService.startRecording(id));
    }

    @PostMapping("/record-stop")
    public ResponseEntity<RecordingResponse> stopRecording(@PathVariable String id) {
        return ResponseEntity.ok(recordingService.stopRecording(id));
    }

    @GetMapping("/recording")
    public ResponseEntity<RecordingResponse> getRecording(@PathVariable String id) {
        return ResponseEntity.ok(recordingService.getRecording(id));
    }
}
