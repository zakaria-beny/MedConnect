package com.medconnect.auditservice.dto;

import java.time.LocalDateTime;

public record AuditLogResponse(
        String id,
        String actorId,
        String action,
        String resourceType,
        String resourceId,
        String details,
        LocalDateTime createdAt
) {
}
