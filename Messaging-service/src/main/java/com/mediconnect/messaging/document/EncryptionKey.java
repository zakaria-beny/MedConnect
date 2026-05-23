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
@Document(collection = "encryption_keys")
public class EncryptionKey {
    @Id
    private String id;
    private String conversationId;
    private String userId;
    private String publicKey;
    private String encryptedPrivateKey;
    private LocalDateTime createdAt;
    private LocalDateTime rotatedAt;
}