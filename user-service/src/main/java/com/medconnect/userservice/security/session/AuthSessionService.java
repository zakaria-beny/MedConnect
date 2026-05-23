package com.medconnect.userservice.security.session;

import com.medconnect.userservice.entity.User;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class AuthSessionService {

    private final AuthSessionRepository authSessionRepository;

    @Value("${medconnect.app.jwtExpirationMs}")
    private int jwtExpirationMs;

    public AuthSessionService(AuthSessionRepository authSessionRepository) {
        this.authSessionRepository = authSessionRepository;
    }

    public AuthSession createSession(User user, HttpServletRequest request) {
        LocalDateTime now = LocalDateTime.now();

        AuthSession session = new AuthSession();
        session.setId(UUID.randomUUID().toString());
        session.setUserId(user.getId());
        session.setEmail(user.getEmail());
        session.setCreatedAt(now);
        session.setLastUsedAt(now);
        session.setExpiresAt(now.plusSeconds(Math.max(1L, jwtExpirationMs / 1000L)));
        session.setRevoked(false);
        session.setIpAddress(extractClientIp(request));
        session.setUserAgent(trim(request != null ? request.getHeader("User-Agent") : null, 512));

        return authSessionRepository.save(session);
    }

    public boolean isSessionActive(String sessionId) {
        return findActiveSession(sessionId).isPresent();
    }

    public Optional<AuthSession> findActiveSession(String sessionId) {
        if (!StringUtils.hasText(sessionId)) {
            return Optional.empty();
        }

        LocalDateTime now = LocalDateTime.now();
        return authSessionRepository.findByIdAndRevokedFalse(sessionId)
                .filter(session -> session.getExpiresAt() != null && session.getExpiresAt().isAfter(now));
    }

    public void touchSession(String sessionId) {
        findActiveSession(sessionId).ifPresent(session -> {
            session.setLastUsedAt(LocalDateTime.now());
            authSessionRepository.save(session);
        });
    }

    public List<AuthSession> getActiveSessionsByUser(String userId) {
        LocalDateTime now = LocalDateTime.now();
        return authSessionRepository.findByUserIdAndRevokedFalseOrderByCreatedAtDesc(userId).stream()
                .filter(session -> session.getExpiresAt() != null && session.getExpiresAt().isAfter(now))
                .toList();
    }

    public boolean revokeSession(String userId, String sessionId) {
        Optional<AuthSession> sessionOpt = authSessionRepository.findByIdAndUserIdAndRevokedFalse(sessionId, userId);
        if (sessionOpt.isEmpty()) {
            return false;
        }
        AuthSession session = sessionOpt.get();
        session.setRevoked(true);
        authSessionRepository.save(session);
        return true;
    }

    public int revokeAllSessionsForUser(String userId) {
        List<AuthSession> activeSessions = authSessionRepository.findByUserIdAndRevokedFalseOrderByCreatedAtDesc(userId);
        activeSessions.forEach(session -> session.setRevoked(true));
        authSessionRepository.saveAll(activeSessions);
        return activeSessions.size();
    }


    private static String extractClientIp(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        String forwarded = request.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(forwarded)) {
            String[] parts = forwarded.split(",");
            if (parts.length > 0) {
                return parts[0].trim();
            }
        }
        return request.getRemoteAddr();
    }

    private static String trim(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }
}
