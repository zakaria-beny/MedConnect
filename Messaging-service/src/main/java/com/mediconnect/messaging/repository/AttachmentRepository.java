package com.mediconnect.messaging.repository;

import com.mediconnect.messaging.document.MessageAttachment;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface AttachmentRepository extends MongoRepository<MessageAttachment, String> {
    List<MessageAttachment> findByConversationId(String conversationId);
    List<MessageAttachment> findByMessageId(String messageId);
}