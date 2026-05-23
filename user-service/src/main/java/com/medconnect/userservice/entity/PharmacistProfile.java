package com.medconnect.userservice.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Document("pharmacist_profiles")
public class PharmacistProfile {
    @Id
    private String id;

    @Indexed(unique = true)
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

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
