package com.mediconnect.dmp.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

/*
 * Consent = a patient granting a doctor access to their medical records.
 *
 * GDPR and French health law require:
 * - Explicit consent before a doctor can access a patient's DMP
 * - The ability to revoke consent at any time
 * - Granular access (e.g., "show only allergies, not full history")
 * - Time-limited access (consent expires)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "consent_records")
public class Consent {

    @Id
    private String id;

    // The patient who is GIVING access
    private String patientId;

    // The doctor who is RECEIVING access
    private String doctorId;

    private String doctorName;

    /*
     * Which sections of the DMP the doctor can access.
     * Examples: ["allergies", "medications", "lab_results"]
     * Null or empty = access to ALL sections
     */
    private List<String> allowedSections;

    // Access level granted
    private AccessLevel accessLevel;

    // When the consent was granted
    private LocalDateTime grantedAt;

    // When the consent expires (null = no expiry)
    private LocalDateTime expiresAt;

    // Whether the consent is currently active
    @Builder.Default
    private boolean active = true;

    // When consent was revoked (null = not revoked)
    private LocalDateTime revokedAt;

    // Reason the patient revoked access (optional)
    private String revocationReason;

    // IP address or device from which consent was granted
    private String grantedFromIp;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    public enum AccessLevel {
        READ_ONLY,      // can only view records
        READ_WRITE,     // can view and add records
        FULL_ACCESS     // complete access including deleting records
    }
}