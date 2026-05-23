package com.medconnect.userservice.security.session;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface AuthSessionRepository extends MongoRepository<AuthSession, String> {
    Optional<AuthSession> findByIdAndRevokedFalse(String id);
    Optional<AuthSession> findByIdAndUserIdAndRevokedFalse(String id, String userId);
    List<AuthSession> findByUserIdAndRevokedFalseOrderByCreatedAtDesc(String userId);
}
