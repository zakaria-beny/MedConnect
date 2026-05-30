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

    @GetMapping("/anomalies")
    public List<AnomalyFlagResponse> getAnomalies() {
        return auditService.getAnomalies();
    }

    @GetMapping("/compliance/status")
    public ComplianceStatusResponse getComplianceStatus() {
        return auditService.getComplianceStatus();
    }

    @PostMapping("/gdpr/export-request")
    public GdprRequestResponse requestExport(@RequestParam String userId) {
        return auditService.requestDataExport(userId);
    }

    @PostMapping("/gdpr/deletion-request")
    public GdprRequestResponse requestDeletion(@RequestParam String userId) {
        return auditService.requestDeletion(userId);
    }

    @DeleteMapping("/gdpr/forget/{userId}")
    public GdprRequestResponse completeDeletion(@PathVariable String userId) {
        return auditService.completeDataDeletion(userId);
    }
}
