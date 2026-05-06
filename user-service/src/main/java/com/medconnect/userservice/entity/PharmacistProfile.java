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

    private String finessNumber;
    private String pharmacyName;
    private String city;
    private String openingHours;
    private boolean deliveryAvailable;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
