package com.medconnect.userservice.dto;

import com.medconnect.userservice.entity.ProfessionalProfileType;
import com.medconnect.userservice.entity.ProfessionalVerificationAuditAction;
import com.medconnect.userservice.entity.ProfessionalVerificationStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ProfessionalVerificationAuditLogResponse {
    private String id;
    private String userId;
    private ProfessionalProfileType profileType;
    private ProfessionalVerificationAuditAction action;
    private String actorUserId;
    private String documentId;
    private ProfessionalVerificationStatus previousStatus;
    private ProfessionalVerificationStatus newStatus;
    private String note;
    private LocalDateTime createdAt;
}
