package com.medconnect.auditservice.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "anomaly_flags")
public class AnomalyFlag {

    @Id
    private String id;
    private String userId;
    private String type;
    private String severity;
    private String description;
    private LocalDateTime detectedAt;
    private boolean resolved;

    public AnomalyFlag() {
    }

    public AnomalyFlag(String id, String userId, String type, String severity, String description, LocalDateTime detectedAt, boolean resolved) {
        this.id = id;
        this.userId = userId;
        this.type = type;
        this.severity = severity;
        this.description = description;
        this.detectedAt = detectedAt;
        this.resolved = resolved;
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getDetectedAt() {
        return detectedAt;
    }

    public void setDetectedAt(LocalDateTime detectedAt) {
        this.detectedAt = detectedAt;
    }

    public boolean isResolved() {
        return resolved;
    }

    public void setResolved(boolean resolved) {
        this.resolved = resolved;
    }
}
