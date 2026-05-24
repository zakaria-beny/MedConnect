package com.medconnect.appointmentservice.controller;

import com.medconnect.appointmentservice.dto.request.WaitListRequest;
import com.medconnect.appointmentservice.service.WaitListService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST Controller for patient wait-list management.
 */
@RestController
@RequestMapping("/api/wait-list")
@RequiredArgsConstructor
public class WaitListController {

    private final WaitListService waitListService;

    /**
     * GET /api/wait-list/{patientId}?doctorId=xxx
     */
    @GetMapping("/{patientId}")
    public ResponseEntity<Map<String, Integer>> getWaitListPosition(
            @PathVariable String patientId,
            @RequestParam String doctorId) {
        int position = waitListService.getWaitListPosition(patientId, doctorId);
        return ResponseEntity.ok(Map.of("position", position));
    }

    /**
     * POST /api/wait-list
     */
    @PostMapping
    public ResponseEntity<Void> addToWaitList(@Valid @RequestBody WaitListRequest request) {
        waitListService.addToWaitList(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * DELETE /api/wait-list/{patientId}?doctorId=xxx
     */
    @DeleteMapping("/{patientId}")
    public ResponseEntity<Void> removeFromWaitList(
            @PathVariable String patientId,
            @RequestParam String doctorId) {
        waitListService.removeFromWaitList(patientId, doctorId);
        return ResponseEntity.noContent().build();
    }
}
