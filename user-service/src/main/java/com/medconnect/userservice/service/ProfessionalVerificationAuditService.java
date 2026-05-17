package com.medconnect.userservice.service;

import com.medconnect.userservice.dto.ProfessionalVerificationAuditLogResponse;
import com.medconnect.userservice.entity.ProfessionalProfileType;
import com.medconnect.userservice.entity.ProfessionalVerificationAuditAction;
import com.medconnect.userservice.entity.ProfessionalVerificationAuditLog;
import com.medconnect.userservice.entity.ProfessionalVerificationStatus;
import com.medconnect.userservice.repository.ProfessionalVerificationAuditLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ProfessionalVerificationAuditService {

    private final ProfessionalVerificationAuditLogRepository repository;

    public ProfessionalVerificationAuditService(ProfessionalVerificationAuditLogRepository repository) {
        this.repository = repository;
    }

    public void logDocumentUploaded(
            String userId,
            ProfessionalProfileType profileType,
            String actorUserId,
            String documentId,
            String note
    ) {
        ProfessionalVerificationAuditLog log = new ProfessionalVerificationAuditLog();
        log.setUserId(userId);
        log.setProfileType(profileType);
        log.setAction(ProfessionalVerificationAuditAction.DOCUMENT_UPLOADED);
        log.setActorUserId(actorUserId);
        log.setDocumentId(documentId);
        log.setNote(trim(note, 500));
        log.setCreatedAt(LocalDateTime.now());
        repository.save(log);
    }

    public void logVerificationStatusChanged(
            String userId,
            ProfessionalProfileType profileType,
            String actorUserId,
            ProfessionalVerificationStatus previousStatus,
            ProfessionalVerificationStatus newStatus,
            String note
    ) {
        ProfessionalVerificationAuditLog log = new ProfessionalVerificationAuditLog();
        log.setUserId(userId);
        log.setProfileType(profileType);
        log.setAction(ProfessionalVerificationAuditAction.VERIFICATION_STATUS_CHANGED);
        log.setActorUserId(actorUserId);
        log.setPreviousStatus(previousStatus);
        log.setNewStatus(newStatus);
        log.setNote(trim(note, 500));
        log.setCreatedAt(LocalDateTime.now());
        repository.save(log);
    }

    public List<ProfessionalVerificationAuditLogResponse> getAuditLogsByUser(String userId) {
        return repository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::toResponse)
                .toList();
    }

    private static String trim(String value, int maxLength) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.length() <= maxLength ? trimmed : trimmed.substring(0, maxLength);
    }

    private ProfessionalVerificationAuditLogResponse toResponse(ProfessionalVerificationAuditLog log) {
        ProfessionalVerificationAuditLogResponse response = new ProfessionalVerificationAuditLogResponse();
        response.setId(log.getId());
        response.setUserId(log.getUserId());
        response.setProfileType(log.getProfileType());
        response.setAction(log.getAction());
        response.setActorUserId(log.getActorUserId());
        response.setDocumentId(log.getDocumentId());
        response.setPreviousStatus(log.getPreviousStatus());
        response.setNewStatus(log.getNewStatus());
        response.setNote(log.getNote());
        response.setCreatedAt(log.getCreatedAt());
        return response;
    }
}
