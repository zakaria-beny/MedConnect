package com.medconnect.teleconsulation.repository;

import com.medconnect.teleconsulation.model.SessionParticipant;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface SessionParticipantRepository extends MongoRepository<SessionParticipant, String> {
    List<SessionParticipant> findBySessionId(String sessionId);
    Optional<SessionParticipant> findBySessionIdAndUserId(String sessionId, String userId);
    long countBySessionId(String sessionId);
    void deleteBySessionIdAndUserId(String sessionId, String userId);
}
