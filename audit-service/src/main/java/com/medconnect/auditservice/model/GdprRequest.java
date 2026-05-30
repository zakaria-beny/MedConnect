package com.medconnect.auditservice.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "gdpr_requests")
public class GdprRequest {

    @Id
    private String id;
    private String userId;
    private String requestType;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;

    public GdprRequest() {
    }

    public GdprRequest(String id, String userId, String requestType, String status, LocalDateTime createdAt, LocalDateTime completedAt) {
        this.id = id;
        this.userId = userId;
        this.requestType = requestType;
        this.status = status;
        this.createdAt = createdAt;
        this.completedAt = completedAt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getRequestType() {
        return requestType;
    }

    public void setRequestType(String requestType) {
        this.requestType = requestType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }
}
