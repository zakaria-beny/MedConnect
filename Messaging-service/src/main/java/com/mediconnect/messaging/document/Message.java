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
@Document(collection = "messages")
public class Message {
    @Id
    private String id;
    private String conversationId;
    private String senderId;
    private String content; // contenu chiffré
    private LocalDateTime sentAt;
    private boolean edited = false;
    private LocalDateTime editedAt;
    private boolean deleted = false;
    private String messageType = "TEXT"; // TEXT, IMAGE, FILE
}