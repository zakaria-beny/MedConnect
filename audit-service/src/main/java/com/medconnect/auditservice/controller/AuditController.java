package com.medconnect.auditservice.controller;

import com.medconnect.auditservice.dto.AuditLogRequest;
import com.medconnect.auditservice.dto.AuditLogResponse;
import com.medconnect.auditservice.dto.AnomalyFlagResponse;
import com.medconnect.auditservice.dto.ComplianceStatusResponse;
import com.medconnect.auditservice.dto.GdprRequestResponse;
import com.medconnect.auditservice.service.AuditService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class AuditController {

    private final AuditService auditService;

    public AuditController(AuditService auditService) {
        this.auditService = auditService;
    }

    @PostMapping("/audit/logs")
    public AuditLogResponse logAction(@Valid @RequestBody AuditLogRequest request) {
        return auditService.logAction(request);
    }

    @GetMapping("/audit/logs")
    public List<AuditLogResponse> getAllLogs() {
        return auditService.getAllLogs();
    }

    @GetMapping("/audit/logs/user/{userId}")
    public List<AuditLogResponse> getLogsByUser(@PathVariable String userId) {
        return auditService.getLogsByUser(userId);
    }

    @GetMapping("/audit/logs/action/{action}")
    public List<AuditLogResponse> getLogsByAction(@PathVariable String action) {
        return auditService.getLogsByAction(action);
    }

    @GetMapping("/audit/logs/patient/{patientId}")
    public List<AuditLogResponse> getLogsByPatient(@PathVariable String patientId) {
        return auditService.getLogsByPatient(patientId);
    }

    @GetMapping("/audit/access-trail/prescription/{prescriptionId}")
    public List<AuditLogResponse> getPrescriptionAuditTrail(@PathVariable String prescriptionId) {
        return auditService.getPrescriptionAuditTrail(prescriptionId);
    }

    @GetMapping("/anomalies")
    public List<AnomalyFlagResponse> getAnomalies() {
        return auditService.getAnomalies();
    }

    @GetMapping("/anomalies/stats")
    public AnomalyFlagResponse getAnomalyStats(@RequestParam(defaultValue = "30d") String period) {
        return auditService.getAnomalyStats(period);
    }

    @GetMapping("/anomalies/user/{userId}")
    public List<AnomalyFlagResponse> getAnomaliesByUser(@PathVariable String userId) {
        return auditService.getAnomaliesByUser(userId);
    }

    @PutMapping("/anomalies/{anomalyId}/resolve")
    public AnomalyFlagResponse resolveAnomaly(@PathVariable String anomalyId, @RequestParam String resolutionNote) {
        return auditService.resolveAnomaly(anomalyId, resolutionNote);
    }

    @GetMapping("/compliance/status")
    public ComplianceStatusResponse getComplianceStatus() {
        return auditService.getComplianceStatus();
    }

    @GetMapping("/compliance/monthly")
    public ComplianceStatusResponse getMonthlyReport(@RequestParam int year, @RequestParam int month) {
        return auditService.getMonthlyReport(year, month);
    }

    @GetMapping("/compliance/annual")
    public ComplianceStatusResponse getAnnualReport(@RequestParam int year) {
        return auditService.getAnnualReport(year);
    }

    @GetMapping("/compliance/data-retention")
    public ComplianceStatusResponse getDataRetentionStatus() {
        return auditService.getDataRetentionStatus();
    }

    @GetMapping("/audit/access/{resourceType}/{resourceId}")
    public List<AuditLogResponse> getResourceAccessLogs(@PathVariable String resourceType, @PathVariable String resourceId) {
        return auditService.getResourceAccessLogs(resourceType, resourceId);
    }

    @GetMapping("/audit/data-access")
    public List<AuditLogResponse> getDataAccessHistory(@RequestParam(required = false) String userId,
                                                         @RequestParam(required = false) String patientId,
                                                         @RequestParam(required = false) String sections,
                                                         @RequestParam(required = false) String startDate,
                                                         @RequestParam(required = false) String endDate) {
        return auditService.getDataAccessHistory(userId, patientId, sections, startDate, endDate);
    }

    @PostMapping("/audit/data-access")
    public AuditLogResponse logDataAccess(@RequestBody AuditLogRequest request) {
        return auditService.logDataAccess(request);
    }

    @PostMapping("/gdpr/export-request")
    public GdprRequestResponse requestExport(@RequestParam String userId, @RequestParam(defaultValue = "json") String format) {
        return auditService.requestDataExport(userId, format);
    }

    @GetMapping("/gdpr/export/{exportId}")
    public GdprRequestResponse getGdprExportStatus(@PathVariable String exportId) {
        return auditService.getGdprExportStatus(exportId);
    }

    @PostMapping("/gdpr/deletion-request")
    public GdprRequestResponse requestDeletion(@RequestParam String userId) {
        return auditService.requestDeletion(userId);
    }

    @GetMapping("/gdpr/deletion-status/{userId}")
    public GdprRequestResponse getGdprDeletionStatus(@PathVariable String userId) {
        return auditService.getGdprDeletionStatus(userId);
    }

    @DeleteMapping("/gdpr/forget/{userId}")
    public GdprRequestResponse completeDeletion(@PathVariable String userId, @RequestParam(required = false) String reason) {
        return auditService.completeDataDeletion(userId, reason);
    }
}
