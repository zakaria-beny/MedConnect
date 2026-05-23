package com.mediconnect.dmp.controller;

import com.mediconnect.dmp.dto.ApiResponse;
import com.mediconnect.dmp.dto.request.ChronicConditionRequest;
import com.mediconnect.dmp.dto.response.ChronicConditionResponse;
import com.mediconnect.dmp.service.ChronicConditionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/dmp/{patientId}/conditions")
@RequiredArgsConstructor
public class ChronicConditionController {

    private final ChronicConditionService conditionService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ChronicConditionResponse>>> getConditions(@PathVariable String patientId) {
        log.info("GET /conditions for patient {}", patientId);
        List<ChronicConditionResponse> conditions = conditionService.getConditions(patientId);
        return ResponseEntity.ok(ApiResponse.success("Chronic conditions retrieved successfully", conditions));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ChronicConditionResponse>> addCondition(
            @PathVariable String patientId,
            @Valid @RequestBody ChronicConditionRequest request) {
        log.info("POST /conditions for patient {}", patientId);
        ChronicConditionResponse response = conditionService.addCondition(patientId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Chronic condition added successfully", response));
    }

    @GetMapping("/{conditionId}")
    public ResponseEntity<ApiResponse<ChronicConditionResponse>> getConditionById(
            @PathVariable String patientId,
            @PathVariable String conditionId) {
        log.info("GET /conditions/{} for patient {}", conditionId, patientId);
        ChronicConditionResponse response = conditionService.getConditionById(conditionId);
        return ResponseEntity.ok(ApiResponse.success("Chronic condition retrieved successfully", response));
    }

    @PutMapping("/{conditionId}")
    public ResponseEntity<ApiResponse<ChronicConditionResponse>> updateCondition(
            @PathVariable String patientId,
            @PathVariable String conditionId,
            @Valid @RequestBody ChronicConditionRequest request) {
        log.info("PUT /conditions/{} for patient {}", conditionId, patientId);
        ChronicConditionResponse response = conditionService.updateCondition(conditionId, request);
        return ResponseEntity.ok(ApiResponse.success("Chronic condition updated successfully", response));
    }

    @DeleteMapping("/{conditionId}")
    public ResponseEntity<ApiResponse<Object>> deleteCondition(
            @PathVariable String patientId,
            @PathVariable String conditionId) {
        log.info("DELETE /conditions/{} for patient {}", conditionId, patientId);
        conditionService.deleteCondition(conditionId);
        return ResponseEntity.ok(ApiResponse.success("Chronic condition deleted successfully"));
    }
}
