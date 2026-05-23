package com.mediconnect.messaging.repository;

import com.mediconnect.messaging.document.Conversation;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface ConversationRepository extends MongoRepository<Conversation, String> {
    List<Conversation> findByParticipantsContaining(String userId);
    List<Conversation> findByParticipantsContainingAndArchivedFalse(String userId);
}