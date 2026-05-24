package com.mediconnect.messaging.service;

import com.mediconnect.messaging.document.ReadReceipt;
import com.mediconnect.messaging.kafka.MessagingKafkaProducer;
import com.mediconnect.messaging.repository.ReadReceiptRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReadReceiptService {

    private final ReadReceiptRepository readReceiptRepository;
    private final MessagingKafkaProducer kafkaProducer;

    public ReadReceipt markAsRead(String messageId, String userId) {
        if (readReceiptRepository.existsByMessageIdAndUserId(messageId, userId)) {
            return readReceiptRepository.findByMessageId(messageId).get(0);
        }
        ReadReceipt receipt = new ReadReceipt();
        receipt.setMessageId(messageId);
        receipt.setUserId(userId);
        receipt.setReadAt(LocalDateTime.now());
        ReadReceipt saved = readReceiptRepository.save(receipt);
        kafkaProducer.publishMessageRead(messageId, userId);
        kafkaProducer.publishMessageDelivered(messageId, userId);
        return saved;
    }

    public List<ReadReceipt> getReadReceipts(String messageId) {
        return readReceiptRepository.findByMessageId(messageId);
    }

    public String showTypingIndicator(String conversationId, String userId) {
        return userId + " est en train d'écrire dans la conversation " + conversationId;
    }
}