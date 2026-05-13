package com.mediconnect.dmp.dto.request;

import com.mediconnect.dmp.model.LabResult;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LabResultRequest {

    @NotBlank(message = "Ordering doctor ID is required")
    private String orderedByDoctorId;

    private String laboratoryName;

    @NotBlank(message = "Test name is required")
    private String testName;

    @NotNull(message = "Lab category is required")
    private LabResult.LabCategory category;

    private LocalDateTime sampleCollectedAt;

    private LocalDateTime resultDate;

    // The individual measured values inside this lab test
    private List<LabResult.LabValue> values;

    private String interpretation;

    // Path to uploaded PDF report file
    private String reportFilePath;

    @NotNull(message = "Result status is required")
    private LabResult.ResultStatus status;
}