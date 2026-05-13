package com.mediconnect.dmp.controller;

import com.mediconnect.dmp.dto.ApiResponse;
import com.mediconnect.dmp.dto.response.*;
import com.mediconnect.dmp.service.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/dmp")
@RequiredArgsConstructor
public class DmpSummaryController {

    private final AllergyService allergyService;
    private final MedicationService medicationService;
    private final ChronicConditionService conditionService;
    private final ConsultationService consultationService;
    private final LabResultService labResultService;
    private final VaccinationService vaccinationService;
    private final DocumentService documentService;
    private final ImagingResultService imagingResultService;
    private final HealthNotebookService healthNotebookService;

    @GetMapping("/{patientId}")
    public ResponseEntity<ApiResponse<DmpSummary>> getDmpSummary(
            @PathVariable String patientId,
            @RequestParam(required = false) List<String> include) {
        log.info("GET /dmp/{} - DMP summary with include: {}", patientId, include);

        DmpSummary.DmpSummaryBuilder summaryBuilder = DmpSummary.builder().patientId(patientId);

        if (include == null || include.contains("allergies")) {
            summaryBuilder.allergies(allergyService.getAllergies(patientId));
        }

        if (include == null || include.contains("medications")) {
            summaryBuilder.currentMedications(medicationService.getCurrentMedications(patientId));
        }

        if (include == null || include.contains("conditions")) {
            summaryBuilder.chronicConditions(conditionService.getConditions(patientId));
        }

        if (include == null || include.contains("consultations")) {
            summaryBuilder.recentConsultations(consultationService.getConsultationHistory(patientId));
        }

        if (include == null || include.contains("lab_results")) {
            summaryBuilder.recentLabResults(labResultService.getLabResults(patientId));
        }

        if (include == null || include.contains("vaccinations")) {
            summaryBuilder.vaccinations(vaccinationService.getVaccinations(patientId));
        }

        if (include == null || include.contains("documents")) {
            summaryBuilder.documents(documentService.getDocuments(patientId));
        }

        if (include == null || include.contains("imaging")) {
            summaryBuilder.imagingResults(imagingResultService.getImagingResults(patientId));
        }

        DmpSummary summary = summaryBuilder.build();
        return ResponseEntity.ok(ApiResponse.success("DMP summary retrieved successfully", summary));
    }

    @GetMapping("/{patientId}/alerts")
    public ResponseEntity<ApiResponse<List<HealthNotebookResponse>>> getAlerts(@PathVariable String patientId) {
        log.info("GET /dmp/{}/alerts for patient {}", patientId, patientId);
        List<HealthNotebookResponse> alerts = healthNotebookService.getAlerts(patientId);
        return ResponseEntity.ok(ApiResponse.success("Alerts retrieved successfully", alerts));
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DmpSummary {
        private String patientId;
        private List<AllergyResponse> allergies;
        private List<MedicationResponse> currentMedications;
        private List<ChronicConditionResponse> chronicConditions;
        private List<ConsultationResponse> recentConsultations;
        private List<LabResultResponse> recentLabResults;
        private List<VaccinationResponse> vaccinations;
        private List<DocumentResponse> documents;
        private List<ImagingResultResponse> imagingResults;
    }
}
