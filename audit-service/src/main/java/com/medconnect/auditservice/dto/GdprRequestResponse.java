package com.medconnect.auditservice.dto;

import java.time.LocalDateTime;

public record GdprRequestResponse(
        String id,
        String userId,
        String requestType,
        String status,
        LocalDateTime createdAt,
        LocalDateTime completedAt
) {
}
