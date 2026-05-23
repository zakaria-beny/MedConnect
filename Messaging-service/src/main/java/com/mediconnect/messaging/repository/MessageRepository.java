package com.mediconnect.messaging.repository;

import com.mediconnect.messaging.document.Message;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface MessageRepository extends MongoRepository<Message, String> {
    List<Message> findByConversationIdOrderBySentAtDesc(String conversationId, Pageable pageable);
    List<Message> findByConversationIdAndContentContaining(String conversationId, String query);
}