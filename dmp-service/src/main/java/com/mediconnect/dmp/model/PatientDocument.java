package com.mediconnect.dmp.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "documents")
public class PatientDocument {

    @Id
    private String id;
    private String patientId;
    private String documentName;
    private String documentType;
    private String fileUrl;
    private String mimeType;
    private Long fileSize;
    private String uploadedBy;
    private String description;
    private boolean virusScanned;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    public enum DocumentType {
        LAB, IMAGING, PRESCRIPTION, REPORT, OTHER
    }
}