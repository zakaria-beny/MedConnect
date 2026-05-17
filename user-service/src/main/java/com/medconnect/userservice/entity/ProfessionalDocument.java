package com.medconnect.userservice.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Document("professional_documents")
public class ProfessionalDocument {
    @Id
    private String id;

    @Indexed
    private String userId;

    private ProfessionalProfileType profileType;
    private ProfessionalDocumentSide side;

    private String originalFilename;
    private String storedFilename;
    private String storagePath;
    private String contentType;
    private long sizeBytes;
    private int version;
    private boolean active;
    private ProfessionalDocumentScanStatus scanStatus;
    private String scanDetail;
    private LocalDateTime scannedAt;
    private boolean encrypted;
    private String encryptionAlgorithm;

    private String uploadedByUserId;
    private LocalDateTime uploadedAt;
}
