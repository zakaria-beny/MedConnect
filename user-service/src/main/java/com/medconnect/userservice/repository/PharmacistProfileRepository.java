package com.medconnect.userservice.repository;

import com.medconnect.userservice.entity.PharmacistProfile;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface PharmacistProfileRepository extends MongoRepository<PharmacistProfile, String> {
    Optional<PharmacistProfile> findByUserId(String userId);
}
