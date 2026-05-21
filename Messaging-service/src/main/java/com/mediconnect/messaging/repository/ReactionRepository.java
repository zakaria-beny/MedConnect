package com.mediconnect.messaging.repository;

import com.mediconnect.messaging.document.MessageReaction;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;
import java.util.Optional;

public interface ReactionRepository extends MongoRepository<MessageReaction, String> {
    List<MessageReaction> findByMessageId(String messageId);
    Optional<MessageReaction> findByMessageIdAndUserIdAndEmoji(String messageId, String userId, String emoji);
}