package com.mediconnect.messaging.service;

import com.mediconnect.messaging.document.Message;
import com.mediconnect.messaging.dto.SendMessageRequest;
import com.mediconnect.messaging.kafka.MessagingKafkaProducer;
import com.mediconnect.messaging.repository.ConversationRepository;
import com.mediconnect.messaging.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository messageRepository;
    private final ConversationRepository conversationRepository;
    private final MessagingKafkaProducer kafkaProducer;

    public Message sendMessage(SendMessageRequest request) {
        Message message = new Message();
        message.setConversationId(request.getConversationId());
        message.setSenderId(request.getSenderId());
        message.setContent(request.getContent());
        message.setMessageType(request.getMessageType());
        message.setSentAt(LocalDateTime.now());

        Message saved = messageRepository.save(message);

        conversationRepository.findById(request.getConversationId()).ifPresent(conv -> {
            conv.setLastMessage(request.getContent());
            conv.setLastMessageAt(LocalDateTime.now());
            conversationRepository.save(conv);
        });

        kafkaProducer.publishMessageSent(saved.getId(), request.getSenderId(), request.getConversationId());
        return saved;
    }

    public Message getMessage(String messageId) {
        return messageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Message non trouvé"));
    }

    public List<Message> getMessages(String conversationId, int page, int size) {
        return messageRepository.findByConversationIdOrderBySentAtDesc(
                conversationId, PageRequest.of(page, size));
    }

    public void deleteMessage(String messageId) {
        Message message = getMessage(messageId);
        if (LocalDateTime.now().isAfter(message.getSentAt().plusHours(1))) {
            throw new RuntimeException("Impossible de supprimer après 1 heure");
        }
        message.setDeleted(true);
        messageRepository.save(message);
    }

    public Message editMessage(String messageId, String newContent) {
        Message message = getMessage(messageId);
        if (LocalDateTime.now().isAfter(message.getSentAt().plusMinutes(15))) {
            throw new RuntimeException("Impossible de modifier après 15 minutes");
        }
        message.setContent(newContent);
        message.setEdited(true);
        message.setEditedAt(LocalDateTime.now());
        return messageRepository.save(message);
    }

    public List<Message> searchMessages(String conversationId, String query) {
        return messageRepository.findByConversationIdAndContentContaining(conversationId, query);
    }
}