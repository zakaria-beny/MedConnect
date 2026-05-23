package com.medconnect.teleconsulation.controller;

import com.medconnect.teleconsulation.dto.request.AdmitPatientRequest;
import com.medconnect.teleconsulation.dto.response.WaitingRoomResponse;
import com.medconnect.teleconsulation.service.WaitingRoomService;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * Waiting room & queue management: join, queue position, admit, doctor's full queue.
 */
@RestController
@RequiredArgsConstructor
@Validated
public class WaitingRoomController {

    private final WaitingRoomService waitingRoomService;

    @PostMapping("/api/teleconsult/sessions/{id}/waiting-room")
    public ResponseEntity<WaitingRoomResponse> joinWaitingRoom(
            @PathVariable String id,
            @RequestParam @NotBlank(message = "patientId is required") String patientId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(waitingRoomService.addToWaitingRoom(id, patientId));
    }

    @GetMapping("/api/teleconsult/sessions/{id}/queue-position")
    public ResponseEntity<WaitingRoomResponse> getQueuePosition(
            @PathVariable String id,
            @RequestParam @NotBlank(message = "patientId is required") String patientId) {
        return ResponseEntity.ok(waitingRoomService.getQueuePosition(patientId));
    }

    @PostMapping("/api/teleconsult/sessions/{id}/admit-next")
    public ResponseEntity<WaitingRoomResponse> admitNext(
            @PathVariable String id,
            @RequestBody(required = false) AdmitPatientRequest request) {
        String patientId = (request != null) ? request.getPatientId() : null;
        return ResponseEntity.ok(waitingRoomService.admitFromWaitingRoom(id, patientId));
    }

    @GetMapping("/api/teleconsult/wait-queue/{doctorId}")
    public ResponseEntity<WaitingRoomResponse> getWaitQueue(@PathVariable String doctorId) {
        return ResponseEntity.ok(waitingRoomService.getWaitQueue(doctorId));
    }
}
