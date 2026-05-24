package com.mediconnect.messaging.repository;

import com.mediconnect.messaging.document.EncryptionKey;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;

public interface EncryptionKeyRepository extends MongoRepository<EncryptionKey, String> {
    Optional<EncryptionKey> findByConversationIdAndUserId(String conversationId, String userId);
}