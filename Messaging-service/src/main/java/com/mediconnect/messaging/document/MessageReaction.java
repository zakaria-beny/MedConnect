package com.mediconnect.messaging.document;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "message_reactions")
public class MessageReaction {
    @Id
    private String id;
    private String messageId;
    private String userId;
    private String emoji;
    private LocalDateTime reactedAt;
}