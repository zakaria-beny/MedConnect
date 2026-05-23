package com.medconnect.userservice.security.mfa;

import com.medconnect.userservice.entity.User;
import com.medconnect.userservice.otp.EmailService;
import com.medconnect.userservice.otp.OtpService;
import com.medconnect.userservice.otp.OtpType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

@Service
public class MfaService {

    private final MfaSettingsRepository mfaSettingsRepository;
    private final OtpService otpService;
    private final EmailService emailService;
    private final TotpService totpService;
    private final TwilioSmsService twilioSmsService;

    @Value("${medconnect.mfa.totp.issuer:MedConnect}")
    private String totpIssuer;

    public MfaService(
            MfaSettingsRepository mfaSettingsRepository,
            OtpService otpService,
            EmailService emailService,
            TotpService totpService,
            TwilioSmsService twilioSmsService
    ) {
        this.mfaSettingsRepository = mfaSettingsRepository;
        this.otpService = otpService;
        this.emailService = emailService;
        this.totpService = totpService;
        this.twilioSmsService = twilioSmsService;
    }

    public MfaMethod parseMethod(String value) {
        if (!StringUtils.hasText(value)) {
            throw new RuntimeException("MFA method is required.");
        }
        try {
            return MfaMethod.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new RuntimeException("Unsupported MFA method: " + value);
        }
    }

    public MfaMethod getLoginMethod(User user) {
        MfaSettings settings = mfaSettingsRepository.findByUserId(user.getId()).orElse(null);
        if (settings == null) {
            return MfaMethod.EMAIL;
        }
        if (settings.isTotpEnabled() && StringUtils.hasText(settings.getTotpSecret())) {
            return MfaMethod.TOTP;
        }
        if (settings.isSmsEnabled() && StringUtils.hasText(settings.getPhoneNumber())) {
            return MfaMethod.SMS;
        }
        return MfaMethod.EMAIL;
    }

    public Map<String, Object> setupTotp(User user) {
        MfaSettings settings = getOrCreate(user.getId());
        String secret = totpService.generateSecret();
        settings.setPendingTotpSecret(secret);
        mfaSettingsRepository.save(settings);

        Map<String, Object> response = new HashMap<>();
        response.put("method", MfaMethod.TOTP.name());
        response.put("secret", secret);
        response.put("otpAuthUri", totpService.buildOtpAuthUri(totpIssuer, user.getEmail(), secret));
        response.put("message", "TOTP setup started. Scan the secret in your authenticator app, then verify code.");
        return response;
    }

    public Map<String, Object> verifyTotpSetup(User user, String code) {
        MfaSettings settings = getOrCreate(user.getId());
        if (!StringUtils.hasText(settings.getPendingTotpSecret())) {
            throw new RuntimeException("No pending TOTP setup found.");
        }
        if (!totpService.verifyCode(settings.getPendingTotpSecret(), code)) {
            throw new RuntimeException("Invalid TOTP code.");
        }

        settings.setTotpSecret(settings.getPendingTotpSecret());
        settings.setPendingTotpSecret(null);
        settings.setTotpEnabled(true);
        settings.setPreferredMethod(MfaMethod.TOTP);
        mfaSettingsRepository.save(settings);

        return Map.of(
                "method", MfaMethod.TOTP.name(),
                "enabled", true,
                "message", "TOTP MFA enabled."
        );
    }

    public Map<String, Object> setupSms(User user, String phoneNumber) {
        String targetPhone = StringUtils.hasText(phoneNumber) ? phoneNumber : user.getTelephone();
        if (!StringUtils.hasText(targetPhone)) {
            throw new RuntimeException("Phone number is required for SMS MFA.");
        }

        MfaSettings settings = getOrCreate(user.getId());
        settings.setPendingPhoneNumber(targetPhone);
        mfaSettingsRepository.save(settings);

        String code = otpService.generateAndSave(user.getEmail(), OtpType.MFA_SMS_SETUP);
        twilioSmsService.sendOtp(targetPhone, code, "verify your MFA phone");

        return Map.of(
                "method", MfaMethod.SMS.name(),
                "phoneNumber", targetPhone,
                "message", "SMS verification code sent."
        );
    }

    public Map<String, Object> verifySmsSetup(User user, String code) {
        MfaSettings settings = getOrCreate(user.getId());
        if (!StringUtils.hasText(settings.getPendingPhoneNumber())) {
            throw new RuntimeException("No pending SMS setup found.");
        }
        if (!otpService.validate(user.getEmail(), OtpType.MFA_SMS_SETUP, code)) {
            throw new RuntimeException("Invalid or expired SMS verification code.");
        }

        settings.setPhoneNumber(settings.getPendingPhoneNumber());
        settings.setPendingPhoneNumber(null);
        settings.setSmsEnabled(true);
        settings.setPreferredMethod(MfaMethod.SMS);
        mfaSettingsRepository.save(settings);

        return Map.of(
                "method", MfaMethod.SMS.name(),
                "enabled", true,
                "phoneNumber", settings.getPhoneNumber(),
                "message", "SMS MFA enabled."
        );
    }

    public void sendLoginChallenge(User user, MfaMethod method) {
        switch (method) {
            case TOTP -> {
                // No outbound challenge; user uses authenticator app.
            }
            case SMS -> {
                MfaSettings settings = getOrCreate(user.getId());
                if (!StringUtils.hasText(settings.getPhoneNumber())) {
                    throw new RuntimeException("SMS MFA is enabled but phone number is missing.");
                }
                String code = otpService.generateAndSave(user.getEmail(), OtpType.MFA_SMS_LOGIN);
                twilioSmsService.sendOtp(settings.getPhoneNumber(), code, "complete your login");
            }
            case EMAIL -> {
                String code = otpService.generateAndSave(user.getEmail(), OtpType.LOGIN_2FA);
                emailService.sendOtp(user.getEmail(), OtpType.LOGIN_2FA, code);
            }
        }
    }

    public boolean verifyLoginChallenge(User user, String code) {
        MfaMethod method = getLoginMethod(user);
        return switch (method) {
            case TOTP -> {
                MfaSettings settings = getOrCreate(user.getId());
                yield StringUtils.hasText(settings.getTotpSecret()) && totpService.verifyCode(settings.getTotpSecret(), code);
            }
            case SMS -> otpService.validate(user.getEmail(), OtpType.MFA_SMS_LOGIN, code);
            case EMAIL -> otpService.validate(user.getEmail(), OtpType.LOGIN_2FA, code);
        };
    }

    private MfaSettings getOrCreate(String userId) {
        return mfaSettingsRepository.findByUserId(userId).orElseGet(() -> {
            MfaSettings settings = new MfaSettings();
            settings.setUserId(userId);
            settings.setPreferredMethod(MfaMethod.EMAIL);
            settings.setTotpEnabled(false);
            settings.setSmsEnabled(false);
            return mfaSettingsRepository.save(settings);
        });
    }
}
