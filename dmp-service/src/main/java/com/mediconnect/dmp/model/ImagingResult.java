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
@Document(collection = "imaging_results")
public class ImagingResult {

    @Id
    private String id;
    private String patientId;
    private String studyType; // CT, MRI, X-RAY, ULTRASOUND, PET, etc.
    private String bodyPart;
    private LocalDateTime studyDate;
    private String performedBy;
    private String radiologist;
    private String dicomPath;
    private String interpretation;
    private String findings;
    private String impression;
    private String recommendations;
    private String status; // PENDING, COMPLETED, REVIEWED
    
    @CreatedDate
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    private LocalDateTime updatedAt;

    public enum ImagingType {
        CT, MRI, X_RAY, ULTRASOUND, PET, MAMMOGRAPHY, FLUOROSCOPY, OTHER
    }

    public enum ImagingStatus {
        PENDING, COMPLETED, REVIEWED, ARCHIVED
    }
}
