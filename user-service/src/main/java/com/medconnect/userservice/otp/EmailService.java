package com.medconnect.userservice.otp;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendOtp(String to, OtpType type, String code) {
        String subject = switch (type) {
            case EMAIL_VERIFICATION -> "[medconnect] Vérifiez votre adresse e-mail";
            case LOGIN_2FA          -> "[medconnect] Code de connexion";
            case PASSWORD_RESET     -> "[medconnect] Réinitialisation de mot de passe";
            case MFA_SMS_SETUP, MFA_SMS_LOGIN -> "[medconnect] Code de sécurité";
        };

        String action = switch (type) {
            case EMAIL_VERIFICATION -> "vérifier votre adresse e-mail";
            case LOGIN_2FA          -> "finaliser votre connexion";
            case PASSWORD_RESET     -> "réinitialiser votre mot de passe";
            case MFA_SMS_SETUP      -> "activer votre MFA par SMS";
            case MFA_SMS_LOGIN      -> "finaliser votre connexion par SMS";
        };

        String html = """
                <div style="font-family:Arial,sans-serif;max-width:480px;margin:0 auto">
                  <h2 style="color:#2c3e50">medconnect — Votre code OTP</h2>
                  <p>Utilisez le code ci-dessous pour %s :</p>
                  <div style="font-size:36px;font-weight:bold;letter-spacing:8px;
                              color:#2980b9;padding:16px;background:#f0f4f8;
                              border-radius:8px;text-align:center">%s</div>
                  <p style="color:#7f8c8d;margin-top:16px">
                    Ce code est valable <strong>10 minutes</strong>.<br>
                    Si vous n'êtes pas à l'origine de cette demande, ignorez cet e-mail.
                  </p>
                </div>
                """.formatted(action, code);

        sendHtmlMessage(to, subject, html, "Failed to send OTP email to " + to);
    }

    public void sendInvitationEmail(String to, String temporaryPassword) {
        String subject = "[medconnect] Invitation de compte";
        String html = """
                <div style="font-family:Arial,sans-serif;max-width:560px;margin:0 auto">
                  <h2 style="color:#2c3e50">medconnect — Invitation</h2>
                  <p>Votre compte a été créé via import. Connectez-vous avec ce mot de passe temporaire :</p>
                  <div style="font-size:24px;font-weight:bold;letter-spacing:3px;
                              color:#2c3e50;padding:12px;background:#f0f4f8;
                              border-radius:8px;text-align:center">%s</div>
                  <p style="margin-top:16px">
                    Pensez à modifier votre mot de passe dès votre première connexion.
                  </p>
                </div>
                """.formatted(temporaryPassword);
        sendHtmlMessage(to, subject, html, "Failed to send invitation email to " + to);
    }

    public void sendClinicInvitation(String to, String clinicName) {
        String subject = "[medconnect] Invitation équipe clinique";
        String html = """
                <div style="font-family:Arial,sans-serif;max-width:560px;margin:0 auto">
                  <h2 style="color:#2c3e50">medconnect — Invitation clinique</h2>
                  <p>Vous avez été ajouté à l'équipe de la clinique <strong>%s</strong>.</p>
                  <p>Connectez-vous à medconnect pour accéder à vos droits d'équipe.</p>
                </div>
                """.formatted(clinicName);
        sendHtmlMessage(to, subject, html, "Failed to send clinic invitation email to " + to);
    }

    private void sendHtmlMessage(String to, String subject, String html, String errorMessage) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(html, true);
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException(errorMessage, e);
        }
    }
}
