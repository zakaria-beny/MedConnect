package com.mediconnect.notification.kafka;

import com.mediconnect.notification.dto.NotificationRequest;
import com.mediconnect.notification.service.DeduplicationService;
import com.mediconnect.notification.service.InAppNotificationService;
import com.mediconnect.notification.service.EmailService;
import com.mediconnect.notification.service.SMSService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class NotificationKafkaConsumer {

    private final InAppNotificationService inAppNotificationService;
    private final EmailService emailService;
    private final SMSService smsService;
    private final DeduplicationService deduplicationService;

    @KafkaListener(topics = "message.sent", groupId = "notification-group")
    public void onMessageSent(Map<String, Object> event) {
        String userId = (String) event.get("senderId");
        String messageId = (String) event.get("messageId");
        if (deduplicationService.isDuplicate(
                userId, "message.sent", messageId)) return;

        NotificationRequest request = buildRequest(
                userId, "MESSAGE", "Nouveau message",
                "Vous avez reçu un nouveau message",
                List.of("IN_APP"), "message.sent", messageId);
        inAppNotificationService.storeNotification(request);
    }

    @KafkaListener(topics = "message.read", groupId = "notification-group")
    public void onMessageRead(Map<String, Object> event) {
        String userId = (String) event.get("userId");
        String messageId = (String) event.get("messageId");

        NotificationRequest request = buildRequest(
                userId, "MESSAGE", "Message lu",
                "Votre message a été lu",
                List.of("IN_APP"), "message.read", messageId);
        inAppNotificationService.storeNotification(request);
    }

    @KafkaListener(topics = "appointment.booked",
            groupId = "notification-group")
    public void onAppointmentBooked(Map<String, Object> event) {
        String userId = (String) event.get("userId");
        String appointmentId = (String) event.get("appointmentId");
        if (deduplicationService.isDuplicate(
                userId, "appointment.booked", appointmentId)) return;

        NotificationRequest request = buildRequest(
                userId, "APPOINTMENT", "Rendez-vous confirmé",
                "Votre rendez-vous a été confirmé",
                List.of("IN_APP", "EMAIL"), "appointment.booked", appointmentId);
        inAppNotificationService.storeNotification(request);
    }

    @KafkaListener(topics = "appointment.cancelled",
            groupId = "notification-group")
    public void onAppointmentCancelled(Map<String, Object> event) {
        String userId = (String) event.get("userId");
        String appointmentId = (String) event.get("appointmentId");

        NotificationRequest request = buildRequest(
                userId, "APPOINTMENT", "Rendez-vous annulé",
                "Votre rendez-vous a été annulé",
                List.of("IN_APP", "EMAIL"),
                "appointment.cancelled", appointmentId);
        inAppNotificationService.storeNotification(request);
    }

    @KafkaListener(topics = "appointment.started",
            groupId = "notification-group")
    public void onAppointmentStarted(Map<String, Object> event) {
        String userId = (String) event.get("userId");
        String appointmentId = (String) event.get("appointmentId");

        NotificationRequest request = buildRequest(
                userId, "APPOINTMENT", "Rendez-vous commencé",
                "Votre rendez-vous a commencé !",
                List.of("IN_APP"),
                "appointment.started", appointmentId);
        inAppNotificationService.storeNotification(request);
    }

    @KafkaListener(topics = "prescription.created",
            groupId = "notification-group")
    public void onPrescriptionCreated(Map<String, Object> event) {
        String userId = (String) event.get("userId");
        String prescriptionId = (String) event.get("prescriptionId");

        NotificationRequest request = buildRequest(
                userId, "PRESCRIPTION", "Nouvelle ordonnance",
                "Une nouvelle ordonnance a été créée pour vous",
                List.of("IN_APP", "EMAIL"),
                "prescription.created", prescriptionId);
        inAppNotificationService.storeNotification(request);
    }

    @KafkaListener(topics = "prescription.dispensed",
            groupId = "notification-group")
    public void onPrescriptionDispensed(Map<String, Object> event) {
        String userId = (String) event.get("userId");
        String prescriptionId = (String) event.get("prescriptionId");

        NotificationRequest request = buildRequest(
                userId, "PRESCRIPTION", "Médicaments disponibles",
                "Vos médicaments sont prêts à être récupérés",
                List.of("IN_APP", "EMAIL"),
                "prescription.dispensed", prescriptionId);
        inAppNotificationService.storeNotification(request);
    }

    @KafkaListener(topics = "drug.interaction.alert",
            groupId = "notification-group")
    public void onDrugInteractionAlert(Map<String, Object> event) {
        String userId = (String) event.get("userId");
        String alertId = (String) event.get("alertId");

        NotificationRequest request = buildRequest(
                userId, "ALERT",
                "⚠️ URGENT - Interaction médicamenteuse",
                "Une interaction médicamenteuse dangereuse détectée !",
                List.of("IN_APP", "EMAIL"),
                "drug.interaction.alert", alertId);
        inAppNotificationService.storeNotification(request);
    }

    @KafkaListener(topics = "allergy.alert",
            groupId = "notification-group")
    public void onAllergyAlert(Map<String, Object> event) {
        String userId = (String) event.get("userId");
        String alertId = (String) event.get("alertId");

        NotificationRequest request = buildRequest(
                userId, "ALERT",
                "⚠️ URGENT - Alerte allergie",
                "Une allergie dangereuse a été détectée !",
                List.of("IN_APP", "EMAIL"),
                "allergy.alert", alertId);
        inAppNotificationService.storeNotification(request);
    }

    @KafkaListener(topics = "user.login", groupId = "notification-group")
    public void onUserLogin(Map<String, Object> event) {
        String userId = (String) event.get("userId");

        NotificationRequest request = buildRequest(
                userId, "SECURITY", "Nouvelle connexion",
                "Une nouvelle connexion détectée sur votre compte",
                List.of("IN_APP"), "user.login", userId);
        inAppNotificationService.storeNotification(request);
    }

    @KafkaListener(topics = "user.logout", groupId = "notification-group")
    public void onUserLogout(Map<String, Object> event) {
        String userId = (String) event.get("userId");

        NotificationRequest request = buildRequest(
                userId, "SECURITY", "Déconnexion",
                "Vous avez été déconnecté",
                List.of("IN_APP"), "user.logout", userId);
        inAppNotificationService.storeNotification(request);
    }

    @KafkaListener(topics = "user.suspended",
            groupId = "notification-group")
    public void onUserSuspended(Map<String, Object> event) {
        String userId = (String) event.get("userId");

        NotificationRequest request = buildRequest(
                userId, "SECURITY", "Compte suspendu",
                "Votre compte a été suspendu. Contactez le support.",
                List.of("IN_APP", "EMAIL"), "user.suspended", userId);
        inAppNotificationService.storeNotification(request);
    }

    @KafkaListener(topics = "password.changed",
            groupId = "notification-group")
    public void onPasswordChanged(Map<String, Object> event) {
        String userId = (String) event.get("userId");

        NotificationRequest request = buildRequest(
                userId, "SECURITY", "Mot de passe modifié",
                "Votre mot de passe a été modifié avec succès",
                List.of("IN_APP", "EMAIL"), "password.changed", userId);
        inAppNotificationService.storeNotification(request);
    }

    @KafkaListener(topics = "teleconsult.started",
            groupId = "notification-group")
    public void onTeleconsultStarted(Map<String, Object> event) {
        String userId = (String) event.get("userId");
        String consultId = (String) event.get("consultId");

        NotificationRequest request = buildRequest(
                userId, "TELECONSULT", "🔴 Téléconsultation en cours",
                "Votre téléconsultation commence maintenant !",
                List.of("IN_APP"), "teleconsult.started", consultId);
        inAppNotificationService.storeNotification(request);
    }

    @KafkaListener(topics = "teleconsult.ended",
            groupId = "notification-group")
    public void onTeleconsultEnded(Map<String, Object> event) {
        String userId = (String) event.get("userId");
        String consultId = (String) event.get("consultId");

        NotificationRequest request = buildRequest(
                userId, "TELECONSULT", "Téléconsultation terminée",
                "Votre téléconsultation est terminée",
                List.of("IN_APP"), "teleconsult.ended", consultId);
        inAppNotificationService.storeNotification(request);
    }

    @KafkaListener(topics = "lab_result.ready",
            groupId = "notification-group")
    public void onLabResultReady(Map<String, Object> event) {
        String userId = (String) event.get("userId");
        String resultId = (String) event.get("resultId");

        NotificationRequest request = buildRequest(
                userId, "LAB", "Résultats disponibles",
                "Vos résultats d'analyses sont disponibles",
                List.of("IN_APP", "EMAIL"), "lab_result.ready", resultId);
        inAppNotificationService.storeNotification(request);
    }

    // Méthode utilitaire pour créer les requêtes
    private NotificationRequest buildRequest(
            String userId, String type, String title,
            String content, List<String> channels,
            String eventType, String resourceId) {
        NotificationRequest request = new NotificationRequest();
        request.setUserId(userId);
        request.setType(type);
        request.setTitle(title);
        request.setContent(content);
        request.setChannels(channels);
        request.setEventType(eventType);
        request.setResourceId(resourceId);
        return request;
    }
}