package com.medconnect.teleconsulation.repository;

import com.medconnect.teleconsulation.model.Recording;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface RecordingRepository extends MongoRepository<Recording, String> {
    Optional<Recording> findBySessionId(String sessionId);
    List<Recording> findByExpiresAtBeforeAndDeletedFalse(LocalDateTime now);
}
