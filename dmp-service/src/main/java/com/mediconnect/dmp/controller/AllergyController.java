package com.mediconnect.dmp.controller;

import com.mediconnect.dmp.dto.ApiResponse;
import com.mediconnect.dmp.dto.request.AllergyRequest;
import com.mediconnect.dmp.dto.response.AllergyResponse;
import com.mediconnect.dmp.service.AllergyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/dmp/{patientId}/allergies")
@RequiredArgsConstructor
public class AllergyController {

    private final AllergyService allergyService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<AllergyResponse>>> getAllergies(@PathVariable String patientId) {
        log.info("GET /allergies for patient {}", patientId);
        List<AllergyResponse> allergies = allergyService.getAllergies(patientId);
        return ResponseEntity.ok(ApiResponse.success("Allergies retrieved successfully", allergies));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<AllergyResponse>> addAllergy(
            @PathVariable String patientId,
            @Valid @RequestBody AllergyRequest request) {
        log.info("POST /allergies for patient {}", patientId);
        AllergyResponse response = allergyService.addAllergy(patientId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Allergy added successfully", response));
    }

    @GetMapping("/{allergyId}")
    public ResponseEntity<ApiResponse<AllergyResponse>> getAllergyById(
            @PathVariable String patientId,
            @PathVariable String allergyId) {
        log.info("GET /allergies/{} for patient {}", allergyId, patientId);
        AllergyResponse response = allergyService.getAllergyById(allergyId);
        return ResponseEntity.ok(ApiResponse.success("Allergy retrieved successfully", response));
    }

    @PutMapping("/{allergyId}")
    public ResponseEntity<ApiResponse<AllergyResponse>> updateAllergy(
            @PathVariable String patientId,
            @PathVariable String allergyId,
            @Valid @RequestBody AllergyRequest request) {
        log.info("PUT /allergies/{} for patient {}", allergyId, patientId);
        AllergyResponse response = allergyService.updateAllergy(allergyId, request);
        return ResponseEntity.ok(ApiResponse.success("Allergy updated successfully", response));
    }

    @DeleteMapping("/{allergyId}")
    public ResponseEntity<ApiResponse<Object>> deleteAllergy(
            @PathVariable String patientId,
            @PathVariable String allergyId) {
        log.info("DELETE /allergies/{} for patient {}", allergyId, patientId);
        allergyService.deleteAllergy(allergyId);
        return ResponseEntity.ok(ApiResponse.success("Allergy deleted successfully"));
    }

    @GetMapping("/check")
    public ResponseEntity<ApiResponse<Boolean>> checkAllergy(
            @PathVariable String patientId,
            @RequestParam String allergen) {
        log.info("GET /allergies/check?allergen={} for patient {}", allergen, patientId);
        boolean exists = allergyService.checkAllergy(patientId, allergen);
        return ResponseEntity.ok(ApiResponse.success("Allergy check completed", exists));
    }
}
