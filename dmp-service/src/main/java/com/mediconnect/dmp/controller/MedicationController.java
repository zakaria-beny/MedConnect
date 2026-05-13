package com.mediconnect.dmp.controller;

import com.mediconnect.dmp.dto.ApiResponse;
import com.mediconnect.dmp.dto.request.MedicationRequest;
import com.mediconnect.dmp.dto.response.MedicationResponse;
import com.mediconnect.dmp.service.MedicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/dmp/{patientId}/medications")
@RequiredArgsConstructor
public class MedicationController {

    private final MedicationService medicationService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<MedicationResponse>>> getAllMedications(@PathVariable String patientId) {
        log.info("GET /medications for patient {}", patientId);
        List<MedicationResponse> medications = medicationService.getAllMedications(patientId);
        return ResponseEntity.ok(ApiResponse.success("Medications retrieved successfully", medications));
    }

    @GetMapping("/current")
    public ResponseEntity<ApiResponse<List<MedicationResponse>>> getCurrentMedications(@PathVariable String patientId) {
        log.info("GET /medications/current for patient {}", patientId);
        List<MedicationResponse> medications = medicationService.getCurrentMedications(patientId);
        return ResponseEntity.ok(ApiResponse.success("Current medications retrieved successfully", medications));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<MedicationResponse>> addMedication(
            @PathVariable String patientId,
            @Valid @RequestBody MedicationRequest request) {
        log.info("POST /medications for patient {}", patientId);
        MedicationResponse response = medicationService.addMedication(patientId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Medication added successfully", response));
    }

    @GetMapping("/{medicationId}")
    public ResponseEntity<ApiResponse<MedicationResponse>> getMedicationById(
            @PathVariable String patientId,
            @PathVariable String medicationId) {
        log.info("GET /medications/{} for patient {}", medicationId, patientId);
        MedicationResponse response = medicationService.getMedicationById(medicationId);
        return ResponseEntity.ok(ApiResponse.success("Medication retrieved successfully", response));
    }

    @PutMapping("/{medicationId}")
    public ResponseEntity<ApiResponse<MedicationResponse>> updateMedication(
            @PathVariable String patientId,
            @PathVariable String medicationId,
            @Valid @RequestBody MedicationRequest request) {
        log.info("PUT /medications/{} for patient {}", medicationId, patientId);
        MedicationResponse response = medicationService.updateMedication(medicationId, request);
        return ResponseEntity.ok(ApiResponse.success("Medication updated successfully", response));
    }

    @PatchMapping("/{medicationId}/stop")
    public ResponseEntity<ApiResponse<MedicationResponse>> stopMedication(
            @PathVariable String patientId,
            @PathVariable String medicationId) {
        log.info("PATCH /medications/{}/stop for patient {}", medicationId, patientId);
        MedicationResponse response = medicationService.stopMedication(medicationId);
        return ResponseEntity.ok(ApiResponse.success("Medication stopped successfully", response));
    }
}
