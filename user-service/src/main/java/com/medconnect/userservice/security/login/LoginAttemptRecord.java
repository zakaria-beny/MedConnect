package com.medconnect.userservice.security.login;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document("login_attempts")
public class LoginAttemptRecord {

    @Id
    private String id;

    @Indexed(unique = true)
    private String email;

    private int attemptsInWindow;
    private int failedAttemptsInWindow;
    private LocalDateTime windowStartedAt;
    private LocalDateTime lastAttemptAt;
    private LocalDateTime lockedUntil;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getAttemptsInWindow() {
        return attemptsInWindow;
    }

    public void setAttemptsInWindow(int attemptsInWindow) {
        this.attemptsInWindow = attemptsInWindow;
    }

    public int getFailedAttemptsInWindow() {
        return failedAttemptsInWindow;
    }

    public void setFailedAttemptsInWindow(int failedAttemptsInWindow) {
        this.failedAttemptsInWindow = failedAttemptsInWindow;
    }

    public LocalDateTime getWindowStartedAt() {
        return windowStartedAt;
    }

    public void setWindowStartedAt(LocalDateTime windowStartedAt) {
        this.windowStartedAt = windowStartedAt;
    }

    public LocalDateTime getLastAttemptAt() {
        return lastAttemptAt;
    }

    public void setLastAttemptAt(LocalDateTime lastAttemptAt) {
        this.lastAttemptAt = lastAttemptAt;
    }

    public LocalDateTime getLockedUntil() {
        return lockedUntil;
    }

    public void setLockedUntil(LocalDateTime lockedUntil) {
        this.lockedUntil = lockedUntil;
    }
}
