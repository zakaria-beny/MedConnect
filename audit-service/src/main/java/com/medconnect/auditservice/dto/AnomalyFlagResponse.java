package com.medconnect.auditservice.dto;

import java.time.LocalDateTime;

public record AnomalyFlagResponse(
        String id,
        String userId,
        String type,
        String severity,
        String description,
        LocalDateTime detectedAt,
        boolean resolved
) {
}
