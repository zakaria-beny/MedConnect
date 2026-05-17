package com.medconnect.userservice.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Document("doctor_profiles")
public class DoctorProfile {
    @Id
    private String id;

    @Indexed(unique = true)
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

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
