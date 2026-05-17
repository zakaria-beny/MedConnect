package com.medconnect.userservice.dto;

import com.medconnect.userservice.entity.ProfessionalVerificationStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PharmacistProfileResponse {
    private String id;
    private String userId;
    private String professionalRegistrationNumber;
    private String nationalIdNumber;
    private String registrationAuthority;
    private String pharmacyName;
    private String city;
    private String openingHours;
    private boolean deliveryAvailable;
    private String cardFrontImageUrl;
    private String cardBackImageUrl;
    private ProfessionalVerificationStatus verificationStatus;
    private String verificationNote;
    private LocalDateTime verifiedAt;
}
