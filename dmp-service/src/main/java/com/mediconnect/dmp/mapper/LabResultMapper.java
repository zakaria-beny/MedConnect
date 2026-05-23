package com.mediconnect.dmp.mapper;

import com.mediconnect.dmp.dto.request.LabResultRequest;
import com.mediconnect.dmp.dto.response.LabResultResponse;
import com.mediconnect.dmp.model.LabResult;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class LabResultMapper {

    public LabResult toModel(LabResultRequest request, String patientId) {
        return LabResult.builder()
                .patientId(patientId)
                .orderedByDoctorId(request.getOrderedByDoctorId())
                .laboratoryName(request.getLaboratoryName())
                .testName(request.getTestName())
                .category(request.getCategory())
                .sampleCollectedAt(request.getSampleCollectedAt())
                .resultDate(request.getResultDate())
                .values(request.getValues())
                .interpretation(request.getInterpretation())
                .reportFilePath(request.getReportFilePath())
                .status(request.getStatus())
                .build();
    }

    public LabResultResponse toResponse(LabResult labResult) {
        return LabResultResponse.builder()
                .id(labResult.getId())
                .patientId(labResult.getPatientId())
                .orderedByDoctorId(labResult.getOrderedByDoctorId())
                .laboratoryName(labResult.getLaboratoryName())
                .testName(labResult.getTestName())
                .category(labResult.getCategory())
                .sampleCollectedAt(labResult.getSampleCollectedAt())
                .resultDate(labResult.getResultDate())
                .values(labResult.getValues())
                .interpretation(labResult.getInterpretation())
                .reportFilePath(labResult.getReportFilePath())
                .status(labResult.getStatus())
                .createdAt(labResult.getCreatedAt())
                .build();
    }

    public List<LabResultResponse> toResponseList(List<LabResult> labResults) {
        return labResults.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
}