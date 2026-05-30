package com.medconnect.auditservice.dto;

import java.time.LocalDateTime;

public record ComplianceStatusResponse(
        long auditLogCount,
        long gdprRequestCount,
        long unresolvedAnomalyCount,
        LocalDateTime generatedAt
) {
}
