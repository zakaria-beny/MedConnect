package com.mediconnect.messaging.controller;

import com.mediconnect.messaging.document.*;
import com.mediconnect.messaging.dto.*;
import com.mediconnect.messaging.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessagingController {

    private final ConversationService conversationService;
    private final MessageService messageService;
    private final ReadReceiptService readReceiptService;
    private final ReactionService reactionService;
    private final AccessControlService accessControlService;
    private final ComplianceService complianceService;
    private final AttachmentService attachmentService;
    private final EncryptionService encryptionService;

    // ===== CONVERSATIONS =====
    @PostMapping("/conversations")
    public ResponseEntity<Conversation> createConversation(
            @Valid @RequestBody CreateConversationRequest request) {
        return ResponseEntity.ok(conversationService.createConversation(request));
    }

    @GetMapping("/conversations")
    public ResponseEntity<List<Conversation>> getConversations(
            @RequestParam String userId) {
        return ResponseEntity.ok(conversationService.getConversations(userId));
    }

    @GetMapping("/conversations/{id}")
    public ResponseEntity<Conversation> getConversation(@PathVariable String id) {
        return ResponseEntity.ok(conversationService.getConversation(id));
    }

    @PutMapping("/conversations/{id}/archive")
    public ResponseEntity<Conversation> archiveConversation(@PathVariable String id) {
        return ResponseEntity.ok(conversationService.archiveConversation(id));
    }

    @PutMapping("/conversations/{id}/unarchive")
    public ResponseEntity<Conversation> unarchiveConversation(@PathVariable String id) {
        return ResponseEntity.ok(conversationService.unarchiveConversation(id));
    }

    @PutMapping("/conversations/{id}/mute")
    public ResponseEntity<Conversation> muteConversation(@PathVariable String id) {
        return ResponseEntity.ok(conversationService.muteConversation(id));
    }

    @PutMapping("/conversations/{id}/pin")
    public ResponseEntity<Conversation> pinConversation(@PathVariable String id) {
        return ResponseEntity.ok(conversationService.pinConversation(id));
    }

    // ===== MESSAGES =====
    @PostMapping("/conversations/{id}/messages")
    public ResponseEntity<Message> sendMessage(
            @Valid @RequestBody SendMessageRequest request) {
        return ResponseEntity.ok(messageService.sendMessage(request));
    }

    @GetMapping("/conversations/{id}/messages")
    public ResponseEntity<List<Message>> getMessages(
            @PathVariable String id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(messageService.getMessages(id, page, size));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteMessage(@PathVariable String id) {
        messageService.deleteMessage(id);
        return ResponseEntity.ok("Message supprimé");
    }

    @PutMapping("/{id}/edit")
    public ResponseEntity<Message> editMessage(
            @PathVariable String id,
            @RequestParam String newContent) {
        return ResponseEntity.ok(messageService.editMessage(id, newContent));
    }

    @GetMapping("/search")
    public ResponseEntity<List<Message>> searchMessages(
            @RequestParam String conversationId,
            @RequestParam String query) {
        return ResponseEntity.ok(messageService.searchMessages(conversationId, query));
    }

    // ===== READ RECEIPTS =====
    @PutMapping("/{id}/read")
    public ResponseEntity<ReadReceipt> markAsRead(
            @PathVariable String id,
            @RequestParam String userId) {
        return ResponseEntity.ok(readReceiptService.markAsRead(id, userId));
    }

    @GetMapping("/{id}/receipts")
    public ResponseEntity<List<ReadReceipt>> getReadReceipts(@PathVariable String id) {
        return ResponseEntity.ok(readReceiptService.getReadReceipts(id));
    }

    // ===== TYPING INDICATOR =====
    @GetMapping("/conversations/{id}/typing")
    public ResponseEntity<String> typingIndicator(
            @PathVariable String id,
            @RequestParam String userId) {
        return ResponseEntity.ok(readReceiptService.showTypingIndicator(id, userId));
    }

    // ===== REACTIONS =====
    @PostMapping("/{id}/react")
    public ResponseEntity<MessageReaction> addReaction(
            @PathVariable String id,
            @RequestParam String userId,
            @RequestParam String emoji) {
        return ResponseEntity.ok(reactionService.addReaction(id, userId, emoji));
    }

    @DeleteMapping("/{id}/react")
    public ResponseEntity<String> removeReaction(
            @PathVariable String id,
            @RequestParam String userId,
            @RequestParam String emoji) {
        reactionService.removeReaction(id, userId, emoji);
        return ResponseEntity.ok("Réaction supprimée");
    }

    @GetMapping("/{id}/reactions")
    public ResponseEntity<List<MessageReaction>> getReactions(@PathVariable String id) {
        return ResponseEntity.ok(reactionService.getReactions(id));
    }

    // ===== ATTACHMENTS =====
    @PostMapping("/conversations/{id}/attachments")
    public ResponseEntity<MessageAttachment> uploadAttachment(
            @PathVariable String id,
            @RequestParam String messageId,
            @RequestParam String fileName,
            @RequestParam String fileType,
            @RequestParam long fileSize,
            @RequestParam String storageUrl) {
        return ResponseEntity.ok(attachmentService.uploadAttachment(
                id, messageId, fileName, fileType, fileSize, storageUrl));
    }

    @GetMapping("/conversations/{id}/attachments")
    public ResponseEntity<List<MessageAttachment>> getAttachments(@PathVariable String id) {
        return ResponseEntity.ok(attachmentService.getAttachments(id));
    }

    @DeleteMapping("/attachments/{attachmentId}")
    public ResponseEntity<String> deleteAttachment(@PathVariable String attachmentId) {
        attachmentService.deleteAttachment(attachmentId);
        return ResponseEntity.ok("Fichier supprimé");
    }

    @PutMapping("/attachments/{attachmentId}/scan")
    public ResponseEntity<String> scanAttachment(@PathVariable String attachmentId) {
        boolean clean = attachmentService.scanForVirus(attachmentId);
        return ResponseEntity.ok(clean ? "Fichier propre ✅" : "Virus détecté ❌");
    }

    // ===== ENCRYPTION =====
    @PostMapping("/conversations/{id}/init-encryption")
    public ResponseEntity<EncryptionKey> initEncryption(
            @PathVariable String id,
            @RequestParam String userId) {
        return ResponseEntity.ok(encryptionService.initializeConversation(id, userId));
    }

    @PutMapping("/conversations/{id}/rotate-keys")
    public ResponseEntity<EncryptionKey> rotateKeys(
            @PathVariable String id,
            @RequestParam String userId) {
        return ResponseEntity.ok(encryptionService.rotateKeys(id, userId));
    }

    // ===== COMPLIANCE =====
    @GetMapping("/conversations/{id}/audit")
    public ResponseEntity<List<Message>> getAuditLog(@PathVariable String id) {
        return ResponseEntity.ok(complianceService.getMessageAuditLog(id));
    }

    @PostMapping("/conversations/{id}/legal-hold")
    public ResponseEntity<String> legalHold(@PathVariable String id) {
        complianceService.handleLegalHold(id);
        return ResponseEntity.ok("Legal hold activé ✅");
    }

    @DeleteMapping("/conversations/{id}/retention")
    public ResponseEntity<String> enforceRetention(@PathVariable String id) {
        complianceService.enforceRetention(id);
        return ResponseEntity.ok("Retention policy appliquée ✅");
    }

    // ===== ACCESS CONTROL =====
    @GetMapping("/conversations/{id}/access")
    public ResponseEntity<Boolean> verifyAccess(
            @PathVariable String id,
            @RequestParam String userId) {
        return ResponseEntity.ok(accessControlService.verifyAccess(userId, id));
    }
}