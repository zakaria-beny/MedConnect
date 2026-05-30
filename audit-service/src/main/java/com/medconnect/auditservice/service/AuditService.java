package com.medconnect.auditservice.service;

import com.medconnect.auditservice.dto.AuditLogRequest;
import com.medconnect.auditservice.dto.AuditLogResponse;
import com.medconnect.auditservice.dto.AnomalyFlagResponse;
import com.medconnect.auditservice.dto.ComplianceStatusResponse;
import com.medconnect.auditservice.dto.GdprRequestResponse;
import com.medconnect.auditservice.kafka.AuditEventPublisher;
import com.medconnect.auditservice.model.AnomalyFlag;
import com.medconnect.auditservice.model.AuditLog;
import com.medconnect.auditservice.model.GdprRequest;
import com.medconnect.auditservice.repository.AnomalyFlagRepository;
import com.medconnect.auditservice.repository.AuditLogRepository;
import com.medconnect.auditservice.repository.GdprRequestRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AuditService {

    private final AuditLogRepository auditLogRepository;
    private final GdprRequestRepository gdprRequestRepository;
    private final AnomalyFlagRepository anomalyFlagRepository;
    private final AuditEventPublisher auditEventPublisher;

    public AuditService(AuditLogRepository auditLogRepository,
                        GdprRequestRepository gdprRequestRepository,
                        AnomalyFlagRepository anomalyFlagRepository,
                        AuditEventPublisher auditEventPublisher) {
        this.auditLogRepository = auditLogRepository;
        this.gdprRequestRepository = gdprRequestRepository;
        this.anomalyFlagRepository = anomalyFlagRepository;
        this.auditEventPublisher = auditEventPublisher;
    }

    public AuditLogResponse logAction(AuditLogRequest request) {
        AuditLog log = new AuditLog();
        log.setActorId(request.getActorId());
        log.setAction(request.getAction());
        log.setResourceType(request.getResourceType());
        log.setResourceId(request.getResourceId());
        log.setDetails(request.getDetails());
        log.setCreatedAt(LocalDateTime.now());
        return toAuditLogResponse(auditLogRepository.save(log));
    }

    public List<AuditLogResponse> getAllLogs() {
        return auditLogRepository.findAll()
                .stream()
                .map(this::toAuditLogResponse)
                .collect(Collectors.toList());
    }

    public List<AuditLogResponse> getLogsByUser(String userId) {
        return auditLogRepository.findByActorIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toAuditLogResponse)
                .collect(Collectors.toList());
    }

    public List<AuditLogResponse> getLogsByAction(String action) {
        return auditLogRepository.findByActionOrderByCreatedAtDesc(action)
                .stream()
                .map(this::toAuditLogResponse)
                .collect(Collectors.toList());
    }

    public GdprRequestResponse requestDataExport(String userId) {
        GdprRequestResponse response = saveGdprRequest(userId, "EXPORT");
        auditEventPublisher.publishGdprExportReady(response.id(), userId, "/api/gdpr/download/" + response.id());
        return response;
    }

    public GdprRequestResponse requestDeletion(String userId) {
        return saveGdprRequest(userId, "DELETION");
    }

    public GdprRequestResponse completeDataDeletion(String userId) {
        Optional<GdprRequest> latest = gdprRequestRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .filter(request -> "DELETION".equals(request.getRequestType()))
                .findFirst();

        if (latest.isEmpty()) {
            return requestDeletion(userId);
        }

        GdprRequest request = latest.get();
        request.setStatus("COMPLETED");
        request.setCompletedAt(LocalDateTime.now());
        GdprRequestResponse response = toGdprResponse(gdprRequestRepository.save(request));
        auditEventPublisher.publishGdprDeletionCompleted(response.id(), userId);
        return response;
    }

    public List<GdprRequestResponse> getGdprRequests(String userId) {
        return gdprRequestRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toGdprResponse)
                .collect(Collectors.toList());
    }

    public List<AnomalyFlagResponse> getAnomalies() {
        return anomalyFlagRepository.findAll()
                .stream()
                .map(this::toAnomalyResponse)
                .collect(Collectors.toList());
    }

    public ComplianceStatusResponse getComplianceStatus() {
        return new ComplianceStatusResponse(
                auditLogRepository.count(),
                gdprRequestRepository.count(),
                anomalyFlagRepository.findByResolvedFalseOrderByDetectedAtDesc().size(),
                LocalDateTime.now()
        );
    }

    public AnomalyFlagResponse flagAnomaly(String userId, String type, String severity, String description) {
        AnomalyFlag anomalyFlag = new AnomalyFlag();
        anomalyFlag.setUserId(userId);
        anomalyFlag.setType(type);
        anomalyFlag.setSeverity(severity);
        anomalyFlag.setDescription(description);
        anomalyFlag.setDetectedAt(LocalDateTime.now());
        anomalyFlag.setResolved(false);
        AnomalyFlagResponse response = toAnomalyResponse(anomalyFlagRepository.save(anomalyFlag));
        auditEventPublisher.publishAnomalyDetected(response.id(), userId, type, description);
        return response;
    }

    private GdprRequestResponse saveGdprRequest(String userId, String requestType) {
        GdprRequest request = new GdprRequest();
        request.setUserId(userId);
        request.setRequestType(requestType);
        request.setStatus("PENDING");
        request.setCreatedAt(LocalDateTime.now());
        request.setCompletedAt(null);
        return toGdprResponse(gdprRequestRepository.save(request));
    }

    private AuditLogResponse toAuditLogResponse(AuditLog log) {
        return new AuditLogResponse(
                log.getId(),
                log.getActorId(),
                log.getAction(),
                log.getResourceType(),
                log.getResourceId(),
                log.getDetails(),
                log.getCreatedAt()
        );
    }

    private GdprRequestResponse toGdprResponse(GdprRequest request) {
        return new GdprRequestResponse(
                request.getId(),
                request.getUserId(),
                request.getRequestType(),
                request.getStatus(),
                request.getCreatedAt(),
                request.getCompletedAt()
        );
    }

    private AnomalyFlagResponse toAnomalyResponse(AnomalyFlag anomalyFlag) {
        return new AnomalyFlagResponse(
                anomalyFlag.getId(),
                anomalyFlag.getUserId(),
                anomalyFlag.getType(),
                anomalyFlag.getSeverity(),
                anomalyFlag.getDescription(),
                anomalyFlag.getDetectedAt(),
                anomalyFlag.isResolved()
        );
    }
}
