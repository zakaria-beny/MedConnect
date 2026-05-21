package com.mediconnect.dmp.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImagingResultRequest {

    @NotBlank(message = "Study type is required")
    private String studyType; // CT, MRI, X-RAY, ULTRASOUND, PET, etc.

    @NotBlank(message = "Body part is required")
    private String bodyPart;

    @NotNull(message = "Study date is required")
    private LocalDateTime studyDate;

    @NotBlank(message = "Performed by is required")
    private String performedBy;

    private String radiologist;

    private String dicomPath;

    private String interpretation;

    private String findings;

    private String impression;

    private String recommendations;

    private String status; // PENDING, COMPLETED, REVIEWED
}
