package com.mediconnect.dmp.controller;

import com.mediconnect.dmp.dto.ApiResponse;
import com.mediconnect.dmp.dto.request.VaccinationRequest;
import com.mediconnect.dmp.dto.response.VaccinationResponse;
import com.mediconnect.dmp.service.VaccinationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/dmp/{patientId}/vaccinations")
@RequiredArgsConstructor
public class VaccinationController {

    private final VaccinationService vaccinationService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<VaccinationResponse>>> getVaccinations(@PathVariable String patientId) {
        log.info("GET /vaccinations for patient {}", patientId);
        List<VaccinationResponse> vaccinations = vaccinationService.getVaccinations(patientId);
        return ResponseEntity.ok(ApiResponse.success("Vaccinations retrieved successfully", vaccinations));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<VaccinationResponse>> addVaccination(
            @PathVariable String patientId,
            @Valid @RequestBody VaccinationRequest request) {
        log.info("POST /vaccinations for patient {}", patientId);
        VaccinationResponse response = vaccinationService.addVaccination(patientId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Vaccination added successfully", response));
    }

    @GetMapping("/{vaccinationId}")
    public ResponseEntity<ApiResponse<VaccinationResponse>> getVaccinationById(
            @PathVariable String patientId,
            @PathVariable String vaccinationId) {
        log.info("GET /vaccinations/{} for patient {}", vaccinationId, patientId);
        VaccinationResponse response = vaccinationService.getVaccinationById(vaccinationId);
        return ResponseEntity.ok(ApiResponse.success("Vaccination retrieved successfully", response));
    }

    @GetMapping("/upcoming")
    public ResponseEntity<ApiResponse<List<VaccinationResponse>>> getUpcomingDoses(@PathVariable String patientId) {
        log.info("GET /vaccinations/upcoming for patient {}", patientId);
        List<VaccinationResponse> vaccinations = vaccinationService.getUpcomingDoses(patientId);
        return ResponseEntity.ok(ApiResponse.success("Upcoming vaccination doses retrieved successfully", vaccinations));
    }

    @DeleteMapping("/{vaccinationId}")
    public ResponseEntity<ApiResponse<Object>> deleteVaccination(
            @PathVariable String patientId,
            @PathVariable String vaccinationId) {
        log.info("DELETE /vaccinations/{} for patient {}", vaccinationId, patientId);
        vaccinationService.deleteVaccination(vaccinationId);
        return ResponseEntity.ok(ApiResponse.success("Vaccination deleted successfully"));
    }
}
