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
@Document(collection = "message_attachments")
public class MessageAttachment {
    @Id
    private String id;
    private String conversationId;
    private String messageId;
    private String fileName;
    private String fileType;
    private long fileSize;
    private String storageUrl;
    private boolean encrypted = true;
    private boolean virusScanned = false;
    private LocalDateTime uploadedAt;
}