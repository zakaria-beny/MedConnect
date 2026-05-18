package com.medconnect.teleconsulation.repository;

import com.medconnect.teleconsulation.model.SessionEvent;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface SessionEventRepository extends MongoRepository<SessionEvent, String> {
    List<SessionEvent> findBySessionId(String sessionId);
    List<SessionEvent> findBySessionIdOrderByTimestampDesc(String sessionId);
}
