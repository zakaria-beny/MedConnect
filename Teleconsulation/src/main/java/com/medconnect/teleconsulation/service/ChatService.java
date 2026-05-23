package com.medconnect.teleconsulation.service;

import com.medconnect.teleconsulation.dto.response.ChatResponse;
import com.medconnect.teleconsulation.model.ChatMessage;
import com.medconnect.teleconsulation.model.SessionChat;
import com.medconnect.teleconsulation.repository.SessionChatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final SessionChatRepository chatRepository;

    public ChatResponse sendMessage(String sessionId, String senderId, String message) {
        SessionChat chat = chatRepository.findBySessionId(sessionId)
                .orElse(SessionChat.builder()
                        .sessionId(sessionId)
                        .messages(new ArrayList<>())
                        .build());

        ChatMessage msg = ChatMessage.builder()
                .senderId(senderId)
                .message(message)
                .sentAt(LocalDateTime.now())
                .build();

        chat.getMessages().add(msg);
        chat.setLastUpdated(LocalDateTime.now());
        chat = chatRepository.save(chat);
        return toResponse(chat);
    }

    public ChatResponse getChat(String sessionId) {
        SessionChat chat = chatRepository.findBySessionId(sessionId)
                .orElse(SessionChat.builder()
                        .sessionId(sessionId)
                        .messages(new ArrayList<>())
                        .build());
        return toResponse(chat);
    }

    public void storeChat(String sessionId, List<ChatMessage> messages) {
        SessionChat chat = chatRepository.findBySessionId(sessionId)
                .orElse(SessionChat.builder()
                        .sessionId(sessionId)
                        .messages(new ArrayList<>())
                        .build());
        chat.getMessages().addAll(messages);
        chat.setLastUpdated(LocalDateTime.now());
        chatRepository.save(chat);
    }

    private ChatResponse toResponse(SessionChat chat) {
        return ChatResponse.builder()
                .sessionId(chat.getSessionId())
                .messages(chat.getMessages())
                .lastUpdated(chat.getLastUpdated())
                .build();
    }
}
