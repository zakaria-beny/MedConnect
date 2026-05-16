package com.mediconnect.prescription.mapper;

import com.mediconnect.prescription.dto.response.RefillHistoryResponse;
import com.mediconnect.prescription.model.RefillHistory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class RefillHistoryMapper {

    public RefillHistoryResponse toResponse(RefillHistory model) {
        return RefillHistoryResponse.builder()
                .id(model.getId())
                .originalPrescriptionId(model.getOriginalPrescriptionId())
                .patientId(model.getPatientId())
                .requestedByPatientId(model.getRequestedByPatientId())
                .approvedByDoctorId(model.getApprovedByDoctorId())
                .status(model.getStatus() != null ? model.getStatus().name() : null)
                .reason(model.getReason())
                .requestedAt(model.getRequestedAt())
                .processedAt(model.getProcessedAt())
                .build();
    }

    public List<RefillHistoryResponse> toResponseList(List<RefillHistory> models) {
        return models.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
}
