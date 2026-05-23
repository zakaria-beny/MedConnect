package com.mediconnect.dmp.dto.response;

import com.mediconnect.dmp.model.Consent;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConsentResponse {
    private String id;
    private String patientId;
    private String doctorId;
    private String doctorName;
    private List<String> allowedSections;
    private Consent.AccessLevel accessLevel;
    private LocalDateTime grantedAt;
    private LocalDateTime expiresAt;
    private boolean active;
    private LocalDateTime revokedAt;
    private String revocationReason;
}