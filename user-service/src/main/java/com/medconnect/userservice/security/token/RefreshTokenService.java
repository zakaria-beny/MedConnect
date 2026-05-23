package com.medconnect.userservice.security.token;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;

@Service
public class RefreshTokenService {

    public static final class ValidationResult {
        private final boolean valid;
        private final String message;
        private final RefreshTokenRecord tokenRecord;

        public ValidationResult(boolean valid, String message, RefreshTokenRecord tokenRecord) {
            this.valid = valid;
            this.message = message;
            this.tokenRecord = tokenRecord;
        }

        public boolean isValid() {
            return valid;
        }

        public String getMessage() {
            return message;
        }

        public RefreshTokenRecord getTokenRecord() {
            return tokenRecord;
        }
    }

    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${medconnect.auth.refresh.expiry-days:7}")
    private int refreshExpiryDays;

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
    }

    public String issueToken(String userId, String sessionId) {
        String rawToken = generateRawToken();
        String tokenHash = sha256(rawToken);

        RefreshTokenRecord token = new RefreshTokenRecord();
        token.setTokenHash(tokenHash);
        token.setUserId(userId);
        token.setSessionId(sessionId);
        token.setCreatedAt(LocalDateTime.now());
        token.setExpiresAt(LocalDateTime.now().plusDays(refreshExpiryDays));
        token.setRevoked(false);

        refreshTokenRepository.save(token);
        return rawToken;
    }

    public ValidationResult validateToken(String rawToken) {
        if (!StringUtils.hasText(rawToken)) {
            return new ValidationResult(false, "Refresh token is required.", null);
        }

        String tokenHash = sha256(rawToken);
        RefreshTokenRecord token = refreshTokenRepository.findByTokenHashAndRevokedFalse(tokenHash).orElse(null);

        if (token == null) {
            return new ValidationResult(false, "Refresh token is invalid.", null);
        }

        if (token.getExpiresAt() == null || !token.getExpiresAt().isAfter(LocalDateTime.now())) {
            token.setRevoked(true);
            refreshTokenRepository.save(token);
            return new ValidationResult(false, "Refresh token has expired.", null);
        }

        return new ValidationResult(true, "Valid", token);
    }

    public String rotateToken(RefreshTokenRecord currentToken) {
        currentToken.setRevoked(true);
        refreshTokenRepository.save(currentToken);
        return issueToken(currentToken.getUserId(), currentToken.getSessionId());
    }

    public void revokeBySession(String sessionId) {
        if (!StringUtils.hasText(sessionId)) {
            return;
        }
        List<RefreshTokenRecord> tokens = refreshTokenRepository.findBySessionIdAndRevokedFalse(sessionId);
        if (tokens.isEmpty()) {
            return;
        }
        tokens.forEach(token -> token.setRevoked(true));
        refreshTokenRepository.saveAll(tokens);
    }

    public void revokeByUserId(String userId) {
        if (!StringUtils.hasText(userId)) {
            return;
        }
        List<RefreshTokenRecord> tokens = refreshTokenRepository.findByUserIdAndRevokedFalse(userId);
        if (tokens.isEmpty()) {
            return;
        }
        tokens.forEach(token -> token.setRevoked(true));
        refreshTokenRepository.saveAll(tokens);
    }


    private static String generateRawToken() {
        return UUID.randomUUID().toString().replace("-", "") + UUID.randomUUID().toString().replace("-", "");
    }

    private static String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
