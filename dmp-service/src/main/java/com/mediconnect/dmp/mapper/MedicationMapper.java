package com.mediconnect.dmp.mapper;

import com.mediconnect.dmp.dto.request.MedicationRequest;
import com.mediconnect.dmp.dto.response.MedicationResponse;
import com.mediconnect.dmp.model.Medication;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class MedicationMapper {

    public Medication toModel(MedicationRequest request, String patientId) {
        return Medication.builder()
                .patientId(patientId)
                .drugName(request.getDrugName())
                .activeIngredient(request.getActiveIngredient())
                .dosage(request.getDosage())
                .frequency(request.getFrequency())
                .route(request.getRoute())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .prescribedBy(request.getPrescribedBy())
                .prescriptionId(request.getPrescriptionId())
                .indication(request.getIndication())
                .refillsRemaining(request.getRefillsRemaining())
                .notes(request.getNotes())
                .status(Medication.MedicationStatus.ACTIVE)
                .build();
    }

    public MedicationResponse toResponse(Medication medication) {
        return MedicationResponse.builder()
                .id(medication.getId())
                .patientId(medication.getPatientId())
                .drugName(medication.getDrugName())
                .activeIngredient(medication.getActiveIngredient())
                .dosage(medication.getDosage())
                .frequency(medication.getFrequency())
                .route(medication.getRoute())
                .startDate(medication.getStartDate())
                .endDate(medication.getEndDate())
                .prescribedBy(medication.getPrescribedBy())
                .prescriptionId(medication.getPrescriptionId())
                .indication(medication.getIndication())
                .refillsRemaining(medication.getRefillsRemaining())
                .lastRefillDate(medication.getLastRefillDate())
                .status(medication.getStatus())
                .notes(medication.getNotes())
                .createdAt(medication.getCreatedAt())
                .updatedAt(medication.getUpdatedAt())
                .build();
    }

    public List<MedicationResponse> toResponseList(List<Medication> medications) {
        return medications.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public void updateModelFromRequest(Medication existing, MedicationRequest request) {
        existing.setDrugName(request.getDrugName());
        existing.setActiveIngredient(request.getActiveIngredient());
        existing.setDosage(request.getDosage());
        existing.setFrequency(request.getFrequency());
        existing.setRoute(request.getRoute());
        existing.setStartDate(request.getStartDate());
        existing.setEndDate(request.getEndDate());
        existing.setPrescribedBy(request.getPrescribedBy());
        existing.setPrescriptionId(request.getPrescriptionId());
        existing.setIndication(request.getIndication());
        existing.setRefillsRemaining(request.getRefillsRemaining());
        existing.setNotes(request.getNotes());
    }
}
