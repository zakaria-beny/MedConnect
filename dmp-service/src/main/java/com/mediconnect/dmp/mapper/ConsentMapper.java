package com.mediconnect.dmp.mapper;

import com.mediconnect.dmp.dto.request.ConsentRequest;
import com.mediconnect.dmp.dto.response.ConsentResponse;
import com.mediconnect.dmp.model.Consent;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ConsentMapper {

    public Consent toModel(ConsentRequest request, String patientId) {
        return Consent.builder()
                .patientId(patientId)
                .doctorId(request.getDoctorId())
                .doctorName(request.getDoctorName())
                .allowedSections(request.getAllowedSections())
                .accessLevel(request.getAccessLevel())
                .grantedAt(LocalDateTime.now())
                .expiresAt(request.getExpiresAt())
                .active(true)
                .build();
    }

    public ConsentResponse toResponse(Consent consent) {
        return ConsentResponse.builder()
                .id(consent.getId())
                .patientId(consent.getPatientId())
                .doctorId(consent.getDoctorId())
                .doctorName(consent.getDoctorName())
                .allowedSections(consent.getAllowedSections())
                .accessLevel(consent.getAccessLevel())
                .grantedAt(consent.getGrantedAt())
                .expiresAt(consent.getExpiresAt())
                .active(consent.isActive())
                .revokedAt(consent.getRevokedAt())
                .revocationReason(consent.getRevocationReason())
                .build();
    }

    public List<ConsentResponse> toResponseList(List<Consent> consents) {
        return consents.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
}
