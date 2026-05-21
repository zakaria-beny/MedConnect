package com.mediconnect.dmp.controller;

import com.mediconnect.dmp.dto.ApiResponse;
import com.mediconnect.dmp.dto.request.HealthNotebookRequest;
import com.mediconnect.dmp.dto.response.HealthNotebookResponse;
import com.mediconnect.dmp.service.HealthNotebookService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/dmp/{patientId}/health-notebook")
@RequiredArgsConstructor
public class HealthNotebookController {

    private final HealthNotebookService healthNotebookService;

    @PostMapping
    public ResponseEntity<ApiResponse<HealthNotebookResponse>> logVitals(
            @PathVariable String patientId,
            @Valid @RequestBody HealthNotebookRequest request) {
        log.info("POST /health-notebook for patient {}", patientId);
        HealthNotebookResponse response = healthNotebookService.logVitals(patientId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Vitals logged successfully", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<HealthNotebookResponse>>> getEntries(@PathVariable String patientId) {
        log.info("GET /health-notebook for patient {}", patientId);
        List<HealthNotebookResponse> entries = healthNotebookService.getEntries(patientId);
        return ResponseEntity.ok(ApiResponse.success("Health notebook entries retrieved successfully", entries));
    }

    @GetMapping("/trends")
    public ResponseEntity<ApiResponse<List<HealthNotebookResponse>>> getTrends(
            @PathVariable String patientId,
            @RequestParam(required = false) String metric,
            @RequestParam(required = false) Integer days,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        log.info("GET /health-notebook/trends for patient {} - metric: {}, days: {}, from: {}, to: {}", 
                 patientId, metric, days, from, to);

        LocalDateTime fromDate = from;
        LocalDateTime toDate = to;

        if (fromDate == null && toDate == null && days != null) {
            toDate = LocalDateTime.now();
            fromDate = toDate.minusDays(days);
        } else if (fromDate == null || toDate == null) {
            toDate = LocalDateTime.now();
            fromDate = toDate.minusDays(30);
        }

        List<HealthNotebookResponse> trends = healthNotebookService.getTrends(patientId, fromDate, toDate);
        return ResponseEntity.ok(ApiResponse.success("Health notebook trends retrieved successfully", trends));
    }

    @GetMapping("/alerts")
    public ResponseEntity<ApiResponse<List<HealthNotebookResponse>>> getAlerts(@PathVariable String patientId) {
        log.info("GET /health-notebook/alerts for patient {}", patientId);
        List<HealthNotebookResponse> alerts = healthNotebookService.getAlerts(patientId);
        return ResponseEntity.ok(ApiResponse.success("Health notebook alerts retrieved successfully", alerts));
    }
}
