package com.mediconnect.notification.controller;

import com.mediconnect.notification.document.Notification;
import com.mediconnect.notification.document.UserPreferences;
import com.mediconnect.notification.dto.NotificationRequest;
import com.mediconnect.notification.dto.PreferencesRequest;
import com.mediconnect.notification.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final InAppNotificationService inAppNotificationService;
    private final PreferencesService preferencesService;
    private final AnalyticsService analyticsService;
    private final QueueManagementService queueManagementService;

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Notification>> getNotifications(
            @PathVariable String userId) {
        return ResponseEntity.ok(
                inAppNotificationService.getNotifications(userId));
    }

    @GetMapping("/user/{userId}/unread")
    public ResponseEntity<List<Notification>> getUnreadNotifications(
            @PathVariable String userId) {
        return ResponseEntity.ok(
                inAppNotificationService.getUnreadNotifications(userId));
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<Notification> markAsRead(@PathVariable String id) {
        return ResponseEntity.ok(inAppNotificationService.markAsRead(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteNotification(@PathVariable String id) {
        inAppNotificationService.deleteNotification(id);
        return ResponseEntity.ok("Notification supprimée");
    }

    @GetMapping("/preferences/{userId}")
    public ResponseEntity<UserPreferences> getPreferences(
            @PathVariable String userId) {
        return ResponseEntity.ok(
                preferencesService.getUserPreferences(userId));
    }

    @PutMapping("/preferences/{userId}")
    public ResponseEntity<UserPreferences> updatePreferences(
            @PathVariable String userId,
            @RequestBody PreferencesRequest request) {
        return ResponseEntity.ok(
                preferencesService.updatePreferences(userId, request));
    }

    @PostMapping("/opt-in/{notificationType}")
    public ResponseEntity<UserPreferences> optIn(
            @PathVariable String notificationType,
            @RequestParam String userId) {
        return ResponseEntity.ok(
                preferencesService.optIn(userId, notificationType));
    }

    @PostMapping("/opt-out/{notificationType}")
    public ResponseEntity<UserPreferences> optOut(
            @PathVariable String notificationType,
            @RequestParam String userId) {
        return ResponseEntity.ok(
                preferencesService.optOut(userId, notificationType));
    }

    @PostMapping("/send")
    public ResponseEntity<String> sendNotification(
            @RequestBody NotificationRequest request) {
        notificationService.sendNotification(request);
        return ResponseEntity.ok("Notification envoyée ✅");
    }

    @GetMapping("/delivery-stats")
    public ResponseEntity<Map<String, Object>> getDeliveryStats() {
        return ResponseEntity.ok(
                analyticsService.getChannelEffectiveness());
    }

    @GetMapping("/analytics/engagement")
    public ResponseEntity<Map<String, Object>> getEngagement() {
        return ResponseEntity.ok(analyticsService.trackEngagementRate());
    }

    @GetMapping("/analytics/optout")
    public ResponseEntity<Map<String, Object>> getOptOutRate() {
        return ResponseEntity.ok(analyticsService.trackOptOutRate());
    }

    @PostMapping("/queue")
    public ResponseEntity<String> enqueueNotification(
            @RequestBody NotificationRequest request) {
        queueManagementService.enqueueNotification(request);
        return ResponseEntity.ok("Notification mise en queue ✅");
    }

    @PostMapping("/queue/process")
    public ResponseEntity<String> processQueue() {
        queueManagementService.processQueue();
        return ResponseEntity.ok("Queue traitée ✅");
    }
}