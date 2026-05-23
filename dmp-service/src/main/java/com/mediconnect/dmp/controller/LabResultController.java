package com.mediconnect.dmp.controller;

import com.mediconnect.dmp.dto.ApiResponse;
import com.mediconnect.dmp.dto.request.LabResultRequest;
import com.mediconnect.dmp.dto.response.LabResultResponse;
import com.mediconnect.dmp.model.LabResult;
import com.mediconnect.dmp.service.LabResultService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/dmp/{patientId}/lab-results")
@RequiredArgsConstructor
public class LabResultController {

    private final LabResultService labResultService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<LabResultResponse>>> getLabResults(@PathVariable String patientId) {
        log.info("GET /lab-results for patient {}", patientId);
        List<LabResultResponse> results = labResultService.getLabResults(patientId);
        return ResponseEntity.ok(ApiResponse.success("Lab results retrieved successfully", results));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<LabResultResponse>> addLabResult(
            @PathVariable String patientId,
            @Valid @RequestBody LabResultRequest request) {
        log.info("POST /lab-results for patient {}", patientId);
        LabResultResponse response = labResultService.addLabResult(patientId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Lab result added successfully", response));
    }

    @GetMapping("/{labResultId}")
    public ResponseEntity<ApiResponse<LabResultResponse>> getLabResultById(
            @PathVariable String patientId,
            @PathVariable String labResultId) {
        log.info("GET /lab-results/{} for patient {}", labResultId, patientId);
        LabResultResponse response = labResultService.getLabResultById(labResultId);
        return ResponseEntity.ok(ApiResponse.success("Lab result retrieved successfully", response));
    }

    @PutMapping("/{labResultId}")
    public ResponseEntity<ApiResponse<LabResultResponse>> updateLabResult(
            @PathVariable String patientId,
            @PathVariable String labResultId,
            @Valid @RequestBody LabResultRequest request) {
        log.info("PUT /lab-results/{} for patient {}", labResultId, patientId);
        LabResultResponse response = labResultService.updateLabResult(labResultId, request);
        return ResponseEntity.ok(ApiResponse.success("Lab result updated successfully", response));
    }

    @GetMapping("/by-category")
    public ResponseEntity<ApiResponse<List<LabResultResponse>>> getLabResultsByCategory(
            @PathVariable String patientId,
            @RequestParam LabResult.LabCategory category) {
        log.info("GET /lab-results/by-category?category={} for patient {}", category, patientId);
        List<LabResultResponse> results = labResultService.getLabResultsByCategory(patientId, category);
        return ResponseEntity.ok(ApiResponse.success("Lab results by category retrieved successfully", results));
    }
}
