package com.medconnect.userservice.security.mfa;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface MfaSettingsRepository extends MongoRepository<MfaSettings, String> {
    Optional<MfaSettings> findByUserId(String userId);
}
