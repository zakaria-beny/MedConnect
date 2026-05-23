package com.mediconnect.dmp.mapper;

import com.mediconnect.dmp.dto.request.ChronicConditionRequest;
import com.mediconnect.dmp.dto.response.ChronicConditionResponse;
import com.mediconnect.dmp.model.ChronicCondition;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ChronicConditionMapper {

    public ChronicCondition toModel(ChronicConditionRequest request, String patientId) {
        return ChronicCondition.builder()
                .patientId(patientId)
                .conditionName(request.getConditionName())
                .icd10Code(request.getIcd10Code())
                .diagnosedDate(request.getDiagnosedDate())
                .diagnosedBy(request.getDiagnosedBy())
                .status(request.getStatus())
                .notes(request.getNotes())
                .treatmentPlan(request.getTreatmentPlan())
                .build();
    }

    public ChronicConditionResponse toResponse(ChronicCondition condition) {
        return ChronicConditionResponse.builder()
                .id(condition.getId())
                .patientId(condition.getPatientId())
                .conditionName(condition.getConditionName())
                .icd10Code(condition.getIcd10Code())
                .diagnosedDate(condition.getDiagnosedDate())
                .diagnosedBy(condition.getDiagnosedBy())
                .status(condition.getStatus())
                .notes(condition.getNotes())
                .treatmentPlan(condition.getTreatmentPlan())
                .lastReviewedDate(condition.getLastReviewedDate())
                .createdAt(condition.getCreatedAt())
                .updatedAt(condition.getUpdatedAt())
                .build();
    }

    public List<ChronicConditionResponse> toResponseList(List<ChronicCondition> conditions) {
        return conditions.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public void updateModelFromRequest(ChronicCondition existing, ChronicConditionRequest request) {
        existing.setConditionName(request.getConditionName());
        existing.setIcd10Code(request.getIcd10Code());
        existing.setDiagnosedDate(request.getDiagnosedDate());
        existing.setDiagnosedBy(request.getDiagnosedBy());
        existing.setStatus(request.getStatus());
        existing.setNotes(request.getNotes());
        existing.setTreatmentPlan(request.getTreatmentPlan());
    }
}
