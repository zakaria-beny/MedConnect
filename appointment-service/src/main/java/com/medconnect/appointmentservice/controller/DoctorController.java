package com.medconnect.appointmentservice.controller;

import com.medconnect.appointmentservice.dto.response.SlotResponse;
import com.medconnect.appointmentservice.service.AvailabilityService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

/**
 * REST Controller for doctor search and availability queries.
 */
@RestController
@RequestMapping("/api/doctors")
@RequiredArgsConstructor
public class DoctorController {

    private final AvailabilityService availabilityService;

    /**
     * GET /api/doctors/search?specialty=Cardiology
     */
    @GetMapping("/search")
    public ResponseEntity<Map<String, String>> searchDoctors(
            @RequestParam(required = false) String specialty) {
        return ResponseEntity.ok(Map.of(
                "message", "Doctor search endpoint — integrate with doctor-service for real data",
                "specialty", specialty != null ? specialty : ""));
    }

    /**
     * GET /api/doctors/{doctorId}/availability
     */
    @GetMapping("/{doctorId}/availability")
    public ResponseEntity<SlotResponse> getDoctorAvailability(@PathVariable String doctorId) {
        Optional<SlotResponse> availability = availabilityService.findNextAvailableSlot(doctorId);
        return availability
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }
}
