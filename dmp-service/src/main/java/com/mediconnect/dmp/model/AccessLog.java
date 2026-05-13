package com.mediconnect.dmp.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

/*
 * AccessLog is IMMUTABLE — we NEVER update or delete these records.
 * Every single time someone views a patient's records, we log it here.
 *
 * This is required by:
 * - French health law (HDS - Hébergeur de Données de Santé)
 * - GDPR Article 30 (records of processing activities)
 *
 * NOTE: No @LastModifiedDate here — this document must NEVER be modified.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "access_logs")
public class AccessLog {

    @Id
    private String id;

    // Which patient's record was accessed
    private String patientId;

    // Who accessed the record (doctor, nurse, admin)
    private String accessedByUserId;
    private String accessedByUserName;
    private String accessedByUserRole;

    // Which sections were viewed
    private List<String> sectionsAccessed;

    // Why they accessed the record (e.g., "Emergency consultation")
    private String reason;

    // What action was performed
    private ActionType action;

    // IP address for security tracking
    private String ipAddress;

    // Device/browser info
    private String userAgent;

    // Was this access authorized (i.e., was there a valid consent)?
    private boolean authorized;

    // If not authorized, why
    private String unauthorizedReason;

    // @CreatedDate = automatically set by Spring when document is saved
    // This is the ONLY timestamp — no updatedAt because this is immutable
    @CreatedDate
    private LocalDateTime accessedAt;

    public enum ActionType {
        VIEW,        // just viewed the record
        CREATE,      // added a new record
        UPDATE,      // modified an existing record
        DELETE,      // removed a record
        EXPORT,      // exported data (FHIR, PDF)
        PRINT        // printed the record
    }
}