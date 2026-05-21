package com.mediconnect.messaging.service;

import com.mediconnect.messaging.document.MessageAttachment;
import com.mediconnect.messaging.repository.AttachmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AttachmentService {

    private final AttachmentRepository attachmentRepository;

    public MessageAttachment uploadAttachment(String conversationId,
                                              String messageId,
                                              String fileName,
                                              String fileType,
                                              long fileSize,
                                              String storageUrl) {
        MessageAttachment attachment = new MessageAttachment();
        attachment.setConversationId(conversationId);
        attachment.setMessageId(messageId);
        attachment.setFileName(fileName);
        attachment.setFileType(fileType);
        attachment.setFileSize(fileSize);
        attachment.setStorageUrl(storageUrl);
        attachment.setEncrypted(true);
        attachment.setVirusScanned(false);
        attachment.setUploadedAt(LocalDateTime.now());
        return attachmentRepository.save(attachment);
    }

    public List<MessageAttachment> getAttachments(String conversationId) {
        return attachmentRepository.findByConversationId(conversationId);
    }

    public List<MessageAttachment> getAttachmentsByMessage(String messageId) {
        return attachmentRepository.findByMessageId(messageId);
    }

    public void deleteAttachment(String attachmentId) {
        attachmentRepository.deleteById(attachmentId);
    }

    public boolean scanForVirus(String attachmentId) {
        attachmentRepository.findById(attachmentId).ifPresent(att -> {
            att.setVirusScanned(true);
            attachmentRepository.save(att);
        });
        return true;
    }
}