package com.medconnect.userservice.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Document("patient_profiles")
public class PatientProfile {
    @Id
    private String id;

    @Indexed(unique = true)
    private String userId;

    private LocalDate dateOfBirth;
    private String bloodType;
    private String insuranceNumber;
    private List<String> allergies;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
