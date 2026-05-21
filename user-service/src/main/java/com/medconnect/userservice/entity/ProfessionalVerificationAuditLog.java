package com.medconnect.userservice.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Document("professional_verification_audit")
public class ProfessionalVerificationAuditLog {
    @Id
    private String id;

    @Indexed
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
