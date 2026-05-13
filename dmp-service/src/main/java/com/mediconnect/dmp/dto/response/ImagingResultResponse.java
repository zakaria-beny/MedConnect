package com.mediconnect.dmp.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImagingResultResponse {

    private String id;
    private String patientId;
    private String studyType;
    private String bodyPart;
    private LocalDateTime studyDate;
    private String performedBy;
    private String radiologist;
    private String dicomPath;
    private String interpretation;
    private String findings;
    private String impression;
    private String recommendations;
    private String status;
    private LocalDateTime createdAt;
}
