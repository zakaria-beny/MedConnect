package com.mediconnect.messaging.service;

import com.mediconnect.messaging.document.Message;
import com.mediconnect.messaging.repository.ConversationRepository;
import com.mediconnect.messaging.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ComplianceService {

    private final MessageRepository messageRepository;
    private final ConversationRepository conversationRepository;

    public List<Message> getMessageAuditLog(String conversationId) {
        return messageRepository.findByConversationIdOrderBySentAtDesc(
                conversationId, PageRequest.of(0, 1000));
    }

    public void enforceRetention(String conversationId) {
        List<Message> messages = messageRepository
                .findByConversationIdOrderBySentAtDesc(
                        conversationId, PageRequest.of(0, 10000));

        LocalDateTime twoYearsAgo = LocalDateTime.now().minusYears(2);

        messages.stream()
                .filter(m -> m.getSentAt().isBefore(twoYearsAgo))
                .forEach(m -> {
                    m.setDeleted(true);
                    messageRepository.save(m);
                });
    }

    public void handleLegalHold(String conversationId) {
        conversationRepository.findById(conversationId).ifPresent(conv -> {
            conv.setExpiryDate(LocalDateTime.now().plusYears(100));
            conversationRepository.save(conv);
            System.out.println("✅ Legal hold activé: " + conversationId);
        });
    }

    public void logMessage(String messageId) {
        System.out.println("📋 Audit log - Message: " + messageId +
                " accédé à: " + LocalDateTime.now());
    }
}