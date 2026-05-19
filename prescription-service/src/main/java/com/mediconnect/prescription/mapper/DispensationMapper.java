package com.mediconnect.prescription.mapper;

import com.mediconnect.prescription.dto.request.DispensedItemRequest;
import com.mediconnect.prescription.dto.response.DispensedItemResponse;
import com.mediconnect.prescription.dto.response.DispensationResponse;
import com.mediconnect.prescription.model.Dispensation;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class DispensationMapper {

    public DispensationResponse toResponse(Dispensation model) {
        List<DispensedItemResponse> items = model.getItems() != null
                ? model.getItems().stream()
                    .map(this::toResponse)
                    .collect(Collectors.toList())
                : null;

        return DispensationResponse.builder()
                .id(model.getId())
                .prescriptionId(model.getPrescriptionId())
                .patientId(model.getPatientId())
                .pharmacyId(model.getPharmacyId())
                .pharmacistId(model.getPharmacistId())
                .dispensedAt(model.getDispensedAt())
                .items(items)
                .status(model.getStatus() != null ? model.getStatus().name() : null)
                .notes(model.getNotes())
                .createdAt(model.getCreatedAt())
                .build();
    }

    public DispensedItemResponse toResponse(Dispensation.DispensedItem model) {
        return DispensedItemResponse.builder()
                .drugName(model.getDrugName())
                .quantityDispensed(model.getQuantityDispensed())
                .quantityRemaining(model.getQuantityRemaining())
                .batchNumber(model.getBatchNumber())
                .build();
    }

    public Dispensation.DispensedItem toModel(DispensedItemRequest request) {
        return Dispensation.DispensedItem.builder()
                .drugName(request.getDrugName())
                .quantityDispensed(request.getQuantityDispensed())
                .batchNumber(request.getBatchNumber())
                .build();
    }

    public List<DispensationResponse> toResponseList(List<Dispensation> models) {
        return models.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
}
