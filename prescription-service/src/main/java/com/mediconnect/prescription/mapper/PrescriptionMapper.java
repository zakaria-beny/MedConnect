package com.mediconnect.prescription.mapper;

import com.mediconnect.prescription.dto.request.PrescriptionItemRequest;
import com.mediconnect.prescription.dto.request.PrescriptionRequest;
import com.mediconnect.prescription.dto.response.PrescriptionItemResponse;
import com.mediconnect.prescription.dto.response.PrescriptionResponse;
import com.mediconnect.prescription.model.Prescription;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class PrescriptionMapper {

    public Prescription toModel(PrescriptionRequest request) {
        List<Prescription.PrescriptionItem> items = request.getItems()
                .stream()
                .map(this::toModel)
                .collect(Collectors.toList());

        return Prescription.builder()
                .patientId(request.getPatientId())
                .doctorId(request.getDoctorId())
                .doctorName(request.getDoctorName())
                .items(items)
                .notes(request.getNotes())
                .refillsAllowed(request.getRefillsAllowed())
                .controlledSubstance(request.isControlledSubstance())
                .build();
    }

    public Prescription.PrescriptionItem toModel(PrescriptionItemRequest request) {
        return Prescription.PrescriptionItem.builder()
                .drugName(request.getDrugName())
                .activeIngredient(request.getActiveIngredient())
                .dosage(request.getDosage())
                .frequency(request.getFrequency())
                .route(request.getRoute())
                .durationDays(request.getDurationDays())
                .instructions(request.getInstructions())
                .quantity(request.getQuantity())
                .substitutionAllowed(request.isSubstitutionAllowed())
                .build();
    }

    public PrescriptionResponse toResponse(Prescription model) {
        List<PrescriptionItemResponse> items = model.getItems()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return PrescriptionResponse.builder()
                .id(model.getId())
                .patientId(model.getPatientId())
                .doctorId(model.getDoctorId())
                .doctorName(model.getDoctorName())
                .items(items)
                .status(model.getStatus() != null ? model.getStatus().name() : null)
                .digitalSignature(model.getDigitalSignature())
                .signedAt(model.getSignedAt())
                .qrCodeBase64(model.getQrCodeBase64())
                .pharmacyId(model.getPharmacyId())
                .sentToPharmacyAt(model.getSentToPharmacyAt())
                .dispensedAt(model.getDispensedAt())
                .dispensedByPharmacistId(model.getDispensedByPharmacistId())
                .refillsAllowed(model.getRefillsAllowed())
                .refillsUsed(model.getRefillsUsed())
                .controlledSubstance(model.isControlledSubstance())
                .notes(model.getNotes())
                .expiresAt(model.getExpiresAt())
                .createdAt(model.getCreatedAt())
                .updatedAt(model.getUpdatedAt())
                .build();
    }

    public PrescriptionItemResponse toResponse(Prescription.PrescriptionItem model) {
        return PrescriptionItemResponse.builder()
                .drugName(model.getDrugName())
                .activeIngredient(model.getActiveIngredient())
                .dosage(model.getDosage())
                .frequency(model.getFrequency())
                .route(model.getRoute())
                .durationDays(model.getDurationDays())
                .instructions(model.getInstructions())
                .quantity(model.getQuantity())
                .substitutionAllowed(model.isSubstitutionAllowed())
                .build();
    }

    public List<PrescriptionResponse> toResponseList(List<Prescription> models) {
        return models.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
}
