package com.mediconnect.messaging.repository;

import com.mediconnect.messaging.document.ReadReceipt;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface ReadReceiptRepository extends MongoRepository<ReadReceipt, String> {
    List<ReadReceipt> findByMessageId(String messageId);
    boolean existsByMessageIdAndUserId(String messageId, String userId);
}