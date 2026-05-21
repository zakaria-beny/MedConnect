package com.mediconnect.dmp.mapper;

import com.mediconnect.dmp.dto.request.AllergyRequest;
import com.mediconnect.dmp.dto.response.AllergyResponse;
import com.mediconnect.dmp.model.Allergy;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class AllergyMapper {

    public Allergy toModel(AllergyRequest request, String patientId) {
        return Allergy.builder()
                .patientId(patientId)
                .allergen(request.getAllergen())
                .severity(request.getSeverity())
                .reaction(request.getReaction())
                .notes(request.getNotes())
                .active(true)
                .build();
    }

    public AllergyResponse toResponse(Allergy allergy) {
        return AllergyResponse.builder()
                .id(allergy.getId())
                .patientId(allergy.getPatientId())
                .allergen(allergy.getAllergen())
                .severity(allergy.getSeverity())
                .reaction(allergy.getReaction())
                .notes(allergy.getNotes())
                .active(allergy.isActive())
                .createdAt(allergy.getCreatedAt())
                .updatedAt(allergy.getUpdatedAt())
                .build();
    }

    public List<AllergyResponse> toResponseList(List<Allergy> allergies) {
        return allergies.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public void updateModelFromRequest(Allergy existing, AllergyRequest request) {
        existing.setAllergen(request.getAllergen());
        existing.setSeverity(request.getSeverity());
        existing.setReaction(request.getReaction());
        existing.setNotes(request.getNotes());
    }
}
