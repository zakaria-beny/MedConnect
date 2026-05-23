package com.medconnect.userservice.repository;

import com.medconnect.userservice.entity.PatientProfile;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface PatientProfileRepository extends MongoRepository<PatientProfile, String> {
    Optional<PatientProfile> findByUserId(String userId);
}
