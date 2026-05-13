package com.mediconnect.dmp.mapper;

import com.mediconnect.dmp.dto.request.ImagingResultRequest;
import com.mediconnect.dmp.dto.response.ImagingResultResponse;
import com.mediconnect.dmp.model.ImagingResult;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ImagingResultMapper {

    public ImagingResult toModel(ImagingResultRequest request, String patientId) {
        return ImagingResult.builder()
                .patientId(patientId)
                .studyType(request.getStudyType())
                .bodyPart(request.getBodyPart())
                .studyDate(request.getStudyDate())
                .performedBy(request.getPerformedBy())
                .radiologist(request.getRadiologist())
                .dicomPath(request.getDicomPath())
                .interpretation(request.getInterpretation())
                .findings(request.getFindings())
                .impression(request.getImpression())
                .recommendations(request.getRecommendations())
                .status(request.getStatus() != null ? request.getStatus() : "COMPLETED")
                .build();
    }

    public ImagingResultResponse toResponse(ImagingResult imagingResult) {
        return ImagingResultResponse.builder()
                .id(imagingResult.getId())
                .patientId(imagingResult.getPatientId())
                .studyType(imagingResult.getStudyType())
                .bodyPart(imagingResult.getBodyPart())
                .studyDate(imagingResult.getStudyDate())
                .performedBy(imagingResult.getPerformedBy())
                .radiologist(imagingResult.getRadiologist())
                .dicomPath(imagingResult.getDicomPath())
                .interpretation(imagingResult.getInterpretation())
                .findings(imagingResult.getFindings())
                .impression(imagingResult.getImpression())
                .recommendations(imagingResult.getRecommendations())
                .status(imagingResult.getStatus())
                .createdAt(imagingResult.getCreatedAt())
                .build();
    }

    public List<ImagingResultResponse> toResponseList(List<ImagingResult> imagingResults) {
        return imagingResults.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
}
