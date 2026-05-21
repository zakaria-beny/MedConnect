package com.mediconnect.dmp.controller;

import com.mediconnect.dmp.dto.ApiResponse;
import com.mediconnect.dmp.dto.request.ConsultationRequest;
import com.mediconnect.dmp.dto.response.ConsultationResponse;
import com.mediconnect.dmp.service.ConsultationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/dmp/{patientId}/consultations")
@RequiredArgsConstructor
public class ConsultationController {

    private final ConsultationService consultationService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ConsultationResponse>>> getConsultationHistory(@PathVariable String patientId) {
        log.info("GET /consultations for patient {}", patientId);
        List<ConsultationResponse> consultations = consultationService.getConsultationHistory(patientId);
        return ResponseEntity.ok(ApiResponse.success("Consultation history retrieved successfully", consultations));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ConsultationResponse>> addConsultation(
            @PathVariable String patientId,
            @Valid @RequestBody ConsultationRequest request) {
        log.info("POST /consultations for patient {}", patientId);
        ConsultationResponse response = consultationService.addConsultation(patientId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Consultation added successfully", response));
    }

    @GetMapping("/{consultationId}")
    public ResponseEntity<ApiResponse<ConsultationResponse>> getConsultationById(
            @PathVariable String patientId,
            @PathVariable String consultationId) {
        log.info("GET /consultations/{} for patient {}", consultationId, patientId);
        ConsultationResponse response = consultationService.getConsultationById(consultationId);
        return ResponseEntity.ok(ApiResponse.success("Consultation retrieved successfully", response));
    }
}
