package com.medconnect.userservice.security.token;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface RefreshTokenRepository extends MongoRepository<RefreshTokenRecord, String> {
    Optional<RefreshTokenRecord> findByTokenHashAndRevokedFalse(String tokenHash);
    List<RefreshTokenRecord> findBySessionIdAndRevokedFalse(String sessionId);
}
