package com.medconnect.userservice.dto;

import com.medconnect.userservice.entity.ProfessionalDocumentSide;
import com.medconnect.userservice.entity.ProfessionalProfileType;
import com.medconnect.userservice.entity.ProfessionalDocumentScanStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ProfessionalDocumentResponse {
    private String id;
    private String userId;
    private ProfessionalProfileType profileType;
    private ProfessionalDocumentSide side;
    private String originalFilename;
    private String contentType;
    private long sizeBytes;
    private int version;
    private boolean active;
    private ProfessionalDocumentScanStatus scanStatus;
    private LocalDateTime uploadedAt;
    private String downloadUrl;
}
