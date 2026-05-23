package com.mediconnect.messaging.service;

import com.mediconnect.messaging.document.MessageReaction;
import com.mediconnect.messaging.repository.ReactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReactionService {

    private final ReactionRepository reactionRepository;

    public MessageReaction addReaction(String messageId, String userId, String emoji) {
        MessageReaction reaction = new MessageReaction();
        reaction.setMessageId(messageId);
        reaction.setUserId(userId);
        reaction.setEmoji(emoji);
        reaction.setReactedAt(LocalDateTime.now());
        return reactionRepository.save(reaction);
    }

    public void removeReaction(String messageId, String userId, String emoji) {
        reactionRepository.findByMessageIdAndUserIdAndEmoji(messageId, userId, emoji)
                .ifPresent(reactionRepository::delete);
    }

    public List<MessageReaction> getReactions(String messageId) {
        return reactionRepository.findByMessageId(messageId);
    }
}