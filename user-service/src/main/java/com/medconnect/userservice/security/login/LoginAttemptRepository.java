package com.medconnect.userservice.security.login;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface LoginAttemptRepository extends MongoRepository<LoginAttemptRecord, String> {
    Optional<LoginAttemptRecord> findByEmail(String email);
}
