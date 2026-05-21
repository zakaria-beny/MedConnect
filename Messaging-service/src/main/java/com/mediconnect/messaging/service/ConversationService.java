package com.mediconnect.messaging.service;

import com.mediconnect.messaging.document.Conversation;
import com.mediconnect.messaging.dto.CreateConversationRequest;
import com.mediconnect.messaging.repository.ConversationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ConversationService {

    private final ConversationRepository conversationRepository;

    public Conversation createConversation(CreateConversationRequest request) {
        Conversation conv = new Conversation();
        conv.setParticipants(request.getParticipants());
        conv.setType(request.getType());
        conv.setCreatedAt(LocalDateTime.now());
        return conversationRepository.save(conv);
    }

    public List<Conversation> getConversations(String userId) {
        return conversationRepository.findByParticipantsContainingAndArchivedFalse(userId);
    }

    public Conversation getConversation(String conversationId) {
        return conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversation non trouvée"));
    }

    public Conversation archiveConversation(String conversationId) {
        Conversation conv = getConversation(conversationId);
        conv.setArchived(true);
        return conversationRepository.save(conv);
    }

    public Conversation unarchiveConversation(String conversationId) {
        Conversation conv = getConversation(conversationId);
        conv.setArchived(false);
        return conversationRepository.save(conv);
    }

    public Conversation muteConversation(String conversationId) {
        Conversation conv = getConversation(conversationId);
        conv.setMuted(true);
        return conversationRepository.save(conv);
    }

    public Conversation pinConversation(String conversationId) {
        Conversation conv = getConversation(conversationId);
        conv.setPinned(true);
        return conversationRepository.save(conv);
    }
}