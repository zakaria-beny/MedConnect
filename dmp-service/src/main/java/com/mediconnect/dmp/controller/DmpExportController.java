package com.mediconnect.dmp.controller;

import com.mediconnect.dmp.dto.ApiResponse;
import com.mediconnect.dmp.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/dmp/export-fhir")
@RequiredArgsConstructor
public class DmpExportController {

    private final AllergyService allergyService;
    private final MedicationService medicationService;
    private final ChronicConditionService chronicConditionService;
    private final ConsultationService consultationService;
    private final LabResultService labResultService;
    private final VaccinationService vaccinationService;
    private final HealthNotebookService healthNotebookService;

    @PostMapping("/{patientId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> exportToFHIR(@PathVariable String patientId) {
        log.info("Exporting DMP to FHIR format for patient {}", patientId);
        
        try {
            Map<String, Object> fhirBundle = new HashMap<>();
            fhirBundle.put("resourceType", "Bundle");
            fhirBundle.put("type", "document");
            fhirBundle.put("patientId", patientId);
            
            // Add all medical records to bundle
            Map<String, Object> entries = new HashMap<>();
            
            entries.put("allergies", allergyService.getAllergies(patientId));
            entries.put("medications", medicationService.getAllMedications(patientId));
            entries.put("conditions", chronicConditionService.getConditions(patientId));
            entries.put("consultations", consultationService.getConsultationHistory(patientId));
            entries.put("labResults", labResultService.getLabResults(patientId));
            entries.put("vaccinations", vaccinationService.getVaccinations(patientId));
            entries.put("healthNotebook", healthNotebookService.getEntries(patientId));
            
            fhirBundle.put("entry", entries);
            
            log.info("FHIR export completed successfully for patient {}", patientId);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("DMP exported to FHIR format successfully", fhirBundle));
            
        } catch (Exception e) {
            log.error("Error exporting DMP to FHIR format for patient {}: {}", patientId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error exporting to FHIR: " + e.getMessage()));
        }
    }
}
