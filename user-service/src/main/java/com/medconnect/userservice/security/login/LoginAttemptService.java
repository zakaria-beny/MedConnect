package com.medconnect.userservice.security.login;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
public class LoginAttemptService {

    public enum AttemptState {
        ALLOWED,
        RATE_LIMITED,
        LOCKED
    }

    public static final class AttemptDecision {
        private final AttemptState state;
        private final long retryAfterSeconds;
        private final String message;

        public AttemptDecision(AttemptState state, long retryAfterSeconds, String message) {
            this.state = state;
            this.retryAfterSeconds = retryAfterSeconds;
            this.message = message;
        }

        public AttemptState getState() {
            return state;
        }

        public long getRetryAfterSeconds() {
            return retryAfterSeconds;
        }

        public String getMessage() {
            return message;
        }

        public boolean isAllowed() {
            return state == AttemptState.ALLOWED;
        }
    }

    private final LoginAttemptRepository loginAttemptRepository;

    @Value("${medconnect.auth.rate-limit.max-attempts:5}")
    private int maxAttemptsPerWindow;

    @Value("${medconnect.auth.rate-limit.window-minutes:15}")
    private int rateLimitWindowMinutes;

    @Value("${medconnect.auth.lockout.max-failed-attempts:5}")
    private int maxFailedAttemptsBeforeLockout;

    @Value("${medconnect.auth.lockout.duration-minutes:15}")
    private int lockoutDurationMinutes;

    public LoginAttemptService(LoginAttemptRepository loginAttemptRepository) {
        this.loginAttemptRepository = loginAttemptRepository;
    }

    public AttemptDecision evaluateAttempt(String email) {
        if (!StringUtils.hasText(email)) {
            return new AttemptDecision(AttemptState.ALLOWED, 0, "Allowed");
        }

        LocalDateTime now = LocalDateTime.now();
        LoginAttemptRecord record = loginAttemptRepository.findByEmail(email).orElse(null);

        if (record == null) {
            return new AttemptDecision(AttemptState.ALLOWED, 0, "Allowed");
        }

        boolean changed = resetWindowIfNeeded(record, now);

        if (record.getLockedUntil() != null && record.getLockedUntil().isAfter(now)) {
            long retryAfter = Math.max(1, Duration.between(now, record.getLockedUntil()).getSeconds());
            return new AttemptDecision(
                    AttemptState.LOCKED,
                    retryAfter,
                    "Account temporarily locked due to repeated failed attempts."
            );
        }

        if (record.getAttemptsInWindow() >= maxAttemptsPerWindow) {
            LocalDateTime windowEnd = record.getWindowStartedAt().plusMinutes(rateLimitWindowMinutes);
            long retryAfter = Math.max(1, Duration.between(now, windowEnd).getSeconds());
            return new AttemptDecision(
                    AttemptState.RATE_LIMITED,
                    retryAfter,
                    "Too many login attempts. Please try again later."
            );
        }

        if (changed) {
            loginAttemptRepository.save(record);
        }
        return new AttemptDecision(AttemptState.ALLOWED, 0, "Allowed");
    }

    public void recordFailedAttempt(String email) {
        if (!StringUtils.hasText(email)) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        LoginAttemptRecord record = loadOrCreate(email, now);
        resetWindowIfNeeded(record, now);

        record.setAttemptsInWindow(record.getAttemptsInWindow() + 1);
        record.setFailedAttemptsInWindow(record.getFailedAttemptsInWindow() + 1);
        record.setLastAttemptAt(now);

        if (record.getFailedAttemptsInWindow() >= maxFailedAttemptsBeforeLockout) {
            record.setLockedUntil(now.plusMinutes(lockoutDurationMinutes));
        }

        loginAttemptRepository.save(record);
    }

    public void recordSuccessfulPrimaryFactor(String email) {
        if (!StringUtils.hasText(email)) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        LoginAttemptRecord record = loadOrCreate(email, now);
        resetWindowIfNeeded(record, now);

        record.setAttemptsInWindow(record.getAttemptsInWindow() + 1);
        record.setFailedAttemptsInWindow(0);
        record.setLockedUntil(null);
        record.setLastAttemptAt(now);

        loginAttemptRepository.save(record);
    }

    public void markLoginCompleted(String email) {
        if (!StringUtils.hasText(email)) {
            return;
        }

        loginAttemptRepository.findByEmail(email).ifPresent(record -> {
            record.setFailedAttemptsInWindow(0);
            record.setLockedUntil(null);
            loginAttemptRepository.save(record);
        });
    }

    private LoginAttemptRecord loadOrCreate(String email, LocalDateTime now) {
        return loginAttemptRepository.findByEmail(email).orElseGet(() -> {
            LoginAttemptRecord record = new LoginAttemptRecord();
            record.setEmail(email);
            record.setAttemptsInWindow(0);
            record.setFailedAttemptsInWindow(0);
            record.setWindowStartedAt(now);
            return record;
        });
    }

    private boolean resetWindowIfNeeded(LoginAttemptRecord record, LocalDateTime now) {
        boolean changed = false;

        if (record.getWindowStartedAt() == null
                || record.getWindowStartedAt().plusMinutes(rateLimitWindowMinutes).isBefore(now)) {
            record.setWindowStartedAt(now);
            record.setAttemptsInWindow(0);
            record.setFailedAttemptsInWindow(0);
            changed = true;
        }

        if (record.getLockedUntil() != null && !record.getLockedUntil().isAfter(now)) {
            record.setLockedUntil(null);
            changed = true;
        }

        return changed;
    }
}
