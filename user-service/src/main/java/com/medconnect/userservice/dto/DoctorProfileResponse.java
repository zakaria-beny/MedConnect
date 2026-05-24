package com.medconnect.userservice.dto;

import com.medconnect.userservice.entity.ProfessionalVerificationStatus;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class DoctorProfileResponse {
    private String id;
    private String userId;
    private String professionalRegistrationNumber;
    private String nationalIdNumber;
    private String registrationAuthority;
    private String specialty;
    private List<String> languages;
    private String city;
    private String clinicName;
    private String cardFrontImageUrl;
    private String cardBackImageUrl;
    private ProfessionalVerificationStatus verificationStatus;
    private String verificationNote;
    private LocalDateTime verifiedAt;
}
