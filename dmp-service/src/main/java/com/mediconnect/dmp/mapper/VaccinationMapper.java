package com.mediconnect.dmp.mapper;

import com.mediconnect.dmp.dto.request.VaccinationRequest;
import com.mediconnect.dmp.dto.response.VaccinationResponse;
import com.mediconnect.dmp.model.Vaccination;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class VaccinationMapper {

    public Vaccination toModel(VaccinationRequest request, String patientId) {
        return Vaccination.builder()
                .patientId(patientId)
                .vaccineName(request.getVaccineName())
                .targetDisease(request.getTargetDisease())
                .administrationDate(request.getAdministrationDate())
                .lotNumber(request.getLotNumber())
                .manufacturer(request.getManufacturer())
                .doseNumber(request.getDoseNumber())
                .totalDosesRequired(request.getTotalDosesRequired())
                .nextDoseDate(request.getNextDoseDate())
                .administeredAt(request.getAdministeredAt())
                .administeredBy(request.getAdministeredBy())
                .adverseReactions(request.getAdverseReactions())
                .build();
    }

    public VaccinationResponse toResponse(Vaccination vaccination) {
        return VaccinationResponse.builder()
                .id(vaccination.getId())
                .patientId(vaccination.getPatientId())
                .vaccineName(vaccination.getVaccineName())
                .targetDisease(vaccination.getTargetDisease())
                .administrationDate(vaccination.getAdministrationDate())
                .lotNumber(vaccination.getLotNumber())
                .manufacturer(vaccination.getManufacturer())
                .doseNumber(vaccination.getDoseNumber())
                .totalDosesRequired(vaccination.getTotalDosesRequired())
                .nextDoseDate(vaccination.getNextDoseDate())
                .administeredAt(vaccination.getAdministeredAt())
                .administeredBy(vaccination.getAdministeredBy())
                .adverseReactions(vaccination.getAdverseReactions())
                .createdAt(vaccination.getCreatedAt())
                .build();
    }

    public List<VaccinationResponse> toResponseList(List<Vaccination> vaccinations) {
        return vaccinations.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
}
