package com.medconnect.userservice.repository;

import com.medconnect.userservice.entity.DoctorProfile;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface DoctorProfileRepository extends MongoRepository<DoctorProfile, String> {
    Optional<DoctorProfile> findByUserId(String userId);
}
