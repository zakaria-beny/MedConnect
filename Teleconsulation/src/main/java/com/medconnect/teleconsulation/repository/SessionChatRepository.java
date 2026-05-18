package com.medconnect.teleconsulation.repository;

import com.medconnect.teleconsulation.model.SessionChat;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface SessionChatRepository extends MongoRepository<SessionChat, String> {
    Optional<SessionChat> findBySessionId(String sessionId);
}
