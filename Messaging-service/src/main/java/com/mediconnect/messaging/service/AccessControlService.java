package com.mediconnect.messaging.service;

import com.mediconnect.messaging.document.Conversation;
import com.mediconnect.messaging.repository.ConversationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AccessControlService {

    private final ConversationRepository conversationRepository;

    public boolean verifyAccess(String userId, String conversationId) {
        return conversationRepository.findById(conversationId)
                .map(conv -> conv.getParticipants().contains(userId))
                .orElse(false);
    }

    public void restrictAccessAfterExpiry(String conversationId, LocalDateTime expiryDate) {
        conversationRepository.findById(conversationId).ifPresent(conv -> {
            conv.setExpiryDate(expiryDate);
            conversationRepository.save(conv);
        });
    }
}