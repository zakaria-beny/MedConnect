package com.medconnect.auditservice.dto;

import jakarta.validation.constraints.NotBlank;

public class AuditLogRequest {
    @NotBlank
    private String actorId;
    @NotBlank
    private String action;
    @NotBlank
    private String resourceType;
    @NotBlank
    private String resourceId;
    private String details;

    public String getActorId() {
        return actorId;
    }

    public void setActorId(String actorId) {
        this.actorId = actorId;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getResourceType() {
        return resourceType;
    }

    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }
}
