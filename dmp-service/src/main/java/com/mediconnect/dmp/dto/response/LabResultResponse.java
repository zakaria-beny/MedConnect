package com.mediconnect.dmp.dto.response;

import com.mediconnect.dmp.model.LabResult;
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
public class LabResultResponse {
    private String id;
    private String patientId;
    private String orderedByDoctorId;
    private String laboratoryName;
    private String testName;
    private LabResult.LabCategory category;
    private LocalDateTime sampleCollectedAt;
    private LocalDateTime resultDate;
    private List<LabResult.LabValue> values;
    private String interpretation;
    private String reportFilePath;
    private LabResult.ResultStatus status;
    private LocalDateTime createdAt;
}