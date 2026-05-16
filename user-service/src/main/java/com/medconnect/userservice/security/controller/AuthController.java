package com.medconnect.userservice.security.controller;

import com.medconnect.userservice.entity.User;
import com.medconnect.userservice.googleAuth.Provider;
import com.medconnect.userservice.googleAuth.dto.GoogleAuthRequest;
import com.medconnect.userservice.googleAuth.google.GoogleAuthService;
import com.medconnect.userservice.otp.EmailService;
import com.medconnect.userservice.otp.OtpService;
import com.medconnect.userservice.otp.OtpType;
import com.medconnect.userservice.otp.dto.ForgotPasswordRequest;
import com.medconnect.userservice.otp.dto.ResetPasswordRequest;
import com.medconnect.userservice.otp.dto.VerifyEmailRequest;
import com.medconnect.userservice.otp.dto.VerifyLoginRequest;
import com.medconnect.userservice.repository.UserRepository;
import com.medconnect.userservice.security.events.AuthEventPublisher;
import com.medconnect.userservice.security.jwt.JwtUtils;
import com.medconnect.userservice.security.login.LoginAttemptService;
import com.medconnect.userservice.security.mfa.MfaMethod;
import com.medconnect.userservice.security.mfa.MfaService;
import com.medconnect.userservice.security.mfa.dto.MfaSetupRequest;
import com.medconnect.userservice.security.mfa.dto.MfaVerifyRequest;
import com.medconnect.userservice.security.payload.JwtResponse;
import com.medconnect.userservice.security.payload.LoginRequest;
import com.medconnect.userservice.security.payload.RefreshTokenRequest;
import com.medconnect.userservice.security.payload.SignupRequest;
import com.medconnect.userservice.security.session.AuthSession;
import com.medconnect.userservice.security.session.AuthSessionService;
import com.medconnect.userservice.security.token.RefreshTokenRecord;
import com.medconnect.userservice.security.token.RefreshTokenService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final GoogleAuthService googleAuthService;
    private final UserRepository userRepository;
    private final PasswordEncoder encoder;
    private final JwtUtils jwtUtils;
    private final OtpService otpService;
    private final EmailService emailService;
    private final LoginAttemptService loginAttemptService;
    private final AuthSessionService authSessionService;
    private final RefreshTokenService refreshTokenService;
    private final MfaService mfaService;
    private final AuthEventPublisher authEventPublisher;

    public AuthController(
            GoogleAuthService googleAuthService,
            UserRepository userRepository,
            PasswordEncoder encoder,
            JwtUtils jwtUtils,
            OtpService otpService,
            EmailService emailService,
            LoginAttemptService loginAttemptService,
            AuthSessionService authSessionService,
            RefreshTokenService refreshTokenService,
            MfaService mfaService,
            AuthEventPublisher authEventPublisher
    ) {
        this.googleAuthService = googleAuthService;
        this.userRepository = userRepository;
        this.encoder = encoder;
        this.jwtUtils = jwtUtils;
        this.otpService = otpService;
        this.emailService = emailService;
        this.loginAttemptService = loginAttemptService;
        this.authSessionService = authSessionService;
        this.refreshTokenService = refreshTokenService;
        this.mfaService = mfaService;
        this.authEventPublisher = authEventPublisher;
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Flow 1 — Signup + Email Verification
    // ──────────────────────────────────────────────────────────────────────────

    @PostMapping({"/signup", "/register"})
    public ResponseEntity<?> signup(@Valid @RequestBody SignupRequest req) {
        if (userRepository.findByEmail(req.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body("Error: Email already in use!");
        }

        User user = new User();
        user.setNom(req.getNom());
        user.setPrenom(req.getPrenom());
        user.setEmail(req.getEmail());
        user.setTelephone(req.getTelephone());
        user.setMotDePasse(encoder.encode(req.getPassword()));
        user.setRoles(List.of("ROLE_USER"));
        user.setProvider(Provider.LOCAL);
        user.setEnabled(false);

        userRepository.save(user);

        String code = otpService.generateAndSave(req.getEmail(), OtpType.EMAIL_VERIFICATION);
        emailService.sendOtp(req.getEmail(), OtpType.EMAIL_VERIFICATION, code);

        return ResponseEntity.ok(Map.of(
                "message", "Account created. Please verify your email with the OTP sent to " + req.getEmail()
        ));
    }

    @PostMapping("/verify-email")
    public ResponseEntity<?> verifyEmail(@Valid @RequestBody VerifyEmailRequest req, HttpServletRequest request) {
        if (!otpService.validate(req.getEmail(), OtpType.EMAIL_VERIFICATION, req.getCode())) {
            return ResponseEntity.badRequest().body("Invalid or expired OTP.");
        }

        User user = userRepository.findByEmail(req.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.isEnabled()) {
            return ResponseEntity.ok(Map.of("message", "Email already verified"));
        }

        user.setEnabled(true);
        userRepository.save(user);

        JwtResponse response = createJwtResponse(user, request);
        authEventPublisher.publishUserLogin(user.getId(), user.getEmail(), response.getSessionId(), "EMAIL_VERIFICATION");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/resend-otp")
    public ResponseEntity<?> resendOtp(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        if (email == null || email.isBlank()) {
            return ResponseEntity.badRequest().body("Email is required.");
        }
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            return ResponseEntity.ok(Map.of("message", "If that email exists, an OTP was sent."));
        }
        if (user.isEnabled()) {
            return ResponseEntity.badRequest().body("Email already verified.");
        }
        String code = otpService.generateAndSave(email, OtpType.EMAIL_VERIFICATION);
        emailService.sendOtp(email, OtpType.EMAIL_VERIFICATION, code);
        return ResponseEntity.ok(Map.of("message", "OTP resent to " + email));
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Flow 2 — Login with 2FA (LOCAL users only)
    // ──────────────────────────────────────────────────────────────────────────

    @PostMapping({"/signin", "/login"})
    public ResponseEntity<?> signin(@Valid @RequestBody LoginRequest req) {
        LoginAttemptService.AttemptDecision attemptDecision = loginAttemptService.evaluateAttempt(req.getEmail());
        if (!attemptDecision.isAllowed()) {
            return blockedAttemptResponse(attemptDecision);
        }

        User user = userRepository.findByEmail(req.getEmail()).orElse(null);

        if (user == null || !encoder.matches(req.getPassword(), user.getMotDePasse())) {
            loginAttemptService.recordFailedAttempt(req.getEmail());
            authEventPublisher.publishAuthFailed(req.getEmail(), "invalid_credentials");
            return ResponseEntity.badRequest().body("Invalid email or password.");
        }

        if (!user.isEnabled()) {
            authEventPublisher.publishAuthFailed(req.getEmail(), "email_not_verified");
            return ResponseEntity.badRequest().body("Email not verified. Please check your inbox.");
        }

        if (user.getProvider() != Provider.LOCAL) {
            authEventPublisher.publishAuthFailed(req.getEmail(), "invalid_provider");
            return ResponseEntity.badRequest().body("Please sign in with Google.");
        }

        loginAttemptService.recordSuccessfulPrimaryFactor(req.getEmail());
        MfaMethod method = mfaService.getLoginMethod(user);
        mfaService.sendLoginChallenge(user, method);
        authEventPublisher.publishMfaRequired(user.getId(), user.getEmail(), method.name());

        return ResponseEntity.ok(Map.of(
                "requiresOtp", true,
                "email", req.getEmail(),
                "mfaMethod", method.name(),
                "message", challengeMessage(method)
        ));
    }

    @PostMapping("/verify-login")
    public ResponseEntity<?> verifyLogin(@Valid @RequestBody VerifyLoginRequest req, HttpServletRequest request) {
        LoginAttemptService.AttemptDecision attemptDecision = loginAttemptService.evaluateAttempt(req.getEmail());
        if (!attemptDecision.isAllowed()) {
            return blockedAttemptResponse(attemptDecision);
        }

        User user = userRepository.findByEmail(req.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!mfaService.verifyLoginChallenge(user, req.getCode())) {
            loginAttemptService.recordFailedAttempt(req.getEmail());
            authEventPublisher.publishAuthFailed(req.getEmail(), "invalid_mfa_code");
            return ResponseEntity.badRequest().body("Invalid or expired OTP.");
        }

        loginAttemptService.markLoginCompleted(req.getEmail());
        MfaMethod method = mfaService.getLoginMethod(user);
        authEventPublisher.publishMfaVerified(user.getId(), user.getEmail(), method.name());

        JwtResponse response = createJwtResponse(user, request);
        authEventPublisher.publishUserLogin(user.getId(), user.getEmail(), response.getSessionId(), method.name());
        return ResponseEntity.ok(response);
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Flow 3 — Password Reset
    // ──────────────────────────────────────────────────────────────────────────

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@Valid @RequestBody ForgotPasswordRequest req) {
        User user = userRepository.findByEmail(req.getEmail()).orElse(null);
        if (user != null && user.getProvider() == Provider.LOCAL) {
            String code = otpService.generateAndSave(req.getEmail(), OtpType.PASSWORD_RESET);
            emailService.sendOtp(req.getEmail(), OtpType.PASSWORD_RESET, code);
        }
        return ResponseEntity.ok(Map.of("message", "If that email exists, an OTP was sent."));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody ResetPasswordRequest req) {
        if (!otpService.validate(req.getEmail(), OtpType.PASSWORD_RESET, req.getCode())) {
            return ResponseEntity.badRequest().body("Invalid or expired OTP.");
        }

        User user = userRepository.findByEmail(req.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setMotDePasse(encoder.encode(req.getNewPassword()));
        userRepository.save(user);

        return ResponseEntity.ok(Map.of("message", "Password updated successfully."));
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Google OAuth
    // ──────────────────────────────────────────────────────────────────────────

    @PostMapping("/google")
    public ResponseEntity<?> googleAuth(@Valid @RequestBody GoogleAuthRequest req, HttpServletRequest request) {
        User user = googleAuthService.loginOrRegister(req.getIdToken());

        JwtResponse response = createJwtResponse(user, request);

        Map<String, Object> resp = new HashMap<>();
        resp.put("accessToken", response.getToken());
        resp.put("refreshToken", response.getRefreshToken());
        resp.put("tokenType", "Bearer");
        resp.put("userId", user.getId());
        resp.put("email", user.getEmail());
        resp.put("roles", user.getRoles());
        resp.put("sessionId", response.getSessionId());
        resp.put("provider", user.getProvider() != null ? user.getProvider().name() : "GOOGLE");
        resp.put("enabled", user.isEnabled());

        authEventPublisher.publishUserLogin(user.getId(), user.getEmail(), response.getSessionId(), "GOOGLE_OAUTH");
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@Valid @RequestBody RefreshTokenRequest req) {
        RefreshTokenService.ValidationResult validation = refreshTokenService.validateToken(req.getRefreshToken());
        if (!validation.isValid()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", validation.getMessage()));
        }

        RefreshTokenRecord tokenRecord = validation.getTokenRecord();
        if (!authSessionService.isSessionActive(tokenRecord.getSessionId())) {
            refreshTokenService.revokeBySession(tokenRecord.getSessionId());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Session expired or revoked."));
        }

        User user = userRepository.findById(tokenRecord.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!user.isEnabled()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "User account is disabled."));
        }

        authSessionService.touchSession(tokenRecord.getSessionId());
        String accessToken = jwtUtils.generateTokenFromEmailAndRoles(user.getEmail(), user.getRoles(), tokenRecord.getSessionId());
        String refreshToken = refreshTokenService.rotateToken(tokenRecord);

        return ResponseEntity.ok(new JwtResponse(
                accessToken,
                user.getId(),
                user.getEmail(),
                user.getRoles(),
                tokenRecord.getSessionId(),
                refreshToken
        ));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(
            Authentication authentication,
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader
    ) {
        User user = getAuthenticatedUser(authentication);
        String sessionId = extractSessionIdFromAuthorization(authorizationHeader);

        if (!StringUtils.hasText(sessionId)) {
            return ResponseEntity.badRequest().body(Map.of("message", "Missing session token context."));
        }

        boolean revoked = authSessionService.revokeSession(user.getId(), sessionId);
        refreshTokenService.revokeBySession(sessionId);
        if (revoked) {
            authEventPublisher.publishUserLogout(user.getId(), user.getEmail(), sessionId);
        }
        return ResponseEntity.ok(Map.of(
                "message", revoked ? "Logged out successfully." : "Session already inactive."
        ));
    }

    @GetMapping("/sessions")
    public ResponseEntity<?> getSessions(Authentication authentication) {
        User user = getAuthenticatedUser(authentication);

        List<Map<String, Object>> sessions = authSessionService.getActiveSessionsByUser(user.getId()).stream()
                .map(this::toSessionView)
                .toList();

        return ResponseEntity.ok(Map.of(
                "sessions", sessions,
                "count", sessions.size()
        ));
    }

    @DeleteMapping("/sessions/{sessionId}")
    public ResponseEntity<?> revokeSession(Authentication authentication, @PathVariable String sessionId) {
        User user = getAuthenticatedUser(authentication);
        boolean revoked = authSessionService.revokeSession(user.getId(), sessionId);
        if (revoked) {
            refreshTokenService.revokeBySession(sessionId);
            authEventPublisher.publishUserLogout(user.getId(), user.getEmail(), sessionId);
        }

        if (!revoked) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Session not found or already inactive."));
        }
        return ResponseEntity.ok(Map.of("message", "Session revoked."));
    }

    @PostMapping("/logout-all-devices")
    public ResponseEntity<?> logoutAllDevices(Authentication authentication) {
        User user = getAuthenticatedUser(authentication);
        int sessionsRevoked = authSessionService.revokeAllSessionsForUser(user.getId());
        refreshTokenService.revokeByUserId(user.getId());
        authEventPublisher.publishUserLogout(user.getId(), user.getEmail(), "all_devices");
        return ResponseEntity.ok(Map.of(
                "message", "All sessions revoked",
                "sessionsRevoked", sessionsRevoked
        ));
    }

    @PostMapping("/mfa/setup")
    public ResponseEntity<?> setupMfa(Authentication authentication, @Valid @RequestBody MfaSetupRequest request) {
        User user = getAuthenticatedUser(authentication);
        MfaMethod method = mfaService.parseMethod(request.getMethod());

        return switch (method) {
            case TOTP -> ResponseEntity.ok(mfaService.setupTotp(user));
            case SMS -> ResponseEntity.ok(mfaService.setupSms(user, request.getPhoneNumber()));
            case EMAIL -> ResponseEntity.badRequest().body(Map.of("message", "EMAIL MFA does not require setup."));
        };
    }

    @PostMapping("/mfa/verify")
    public ResponseEntity<?> verifyMfa(Authentication authentication, @Valid @RequestBody MfaVerifyRequest request) {
        User user = getAuthenticatedUser(authentication);
        MfaMethod method = mfaService.parseMethod(request.getMethod());

        Map<String, Object> response = switch (method) {
            case TOTP -> mfaService.verifyTotpSetup(user, request.getCode());
            case SMS -> mfaService.verifySmsSetup(user, request.getCode());
            case EMAIL -> throw new RuntimeException("EMAIL MFA does not require verification setup.");
        };

        authEventPublisher.publishMfaVerified(user.getId(), user.getEmail(), method.name());
        return ResponseEntity.ok(response);
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Helpers
    // ──────────────────────────────────────────────────────────────────────────

    private ResponseEntity<?> blockedAttemptResponse(LoginAttemptService.AttemptDecision decision) {
        HttpStatus status = decision.getState() == LoginAttemptService.AttemptState.LOCKED
                ? HttpStatus.LOCKED
                : HttpStatus.TOO_MANY_REQUESTS;

        return ResponseEntity.status(status).body(Map.of(
                "message", decision.getMessage(),
                "retryAfterSeconds", decision.getRetryAfterSeconds()
        ));
    }

    private JwtResponse createJwtResponse(User user, HttpServletRequest request) {
        AuthSession session = authSessionService.createSession(user, request);
        String jwt = jwtUtils.generateTokenFromEmailAndRoles(user.getEmail(), user.getRoles(), session.getId());
        String refreshToken = refreshTokenService.issueToken(user.getId(), session.getId());
        return new JwtResponse(jwt, user.getId(), user.getEmail(), user.getRoles(), session.getId(), refreshToken);
    }

    private User getAuthenticatedUser(Authentication authentication) {
        if (authentication == null || !StringUtils.hasText(authentication.getName())) {
            throw new RuntimeException("Unauthorized");
        }
        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    private String extractSessionIdFromAuthorization(String authorizationHeader) {
        if (!StringUtils.hasText(authorizationHeader) || !authorizationHeader.startsWith("Bearer ")) {
            return null;
        }
        String token = authorizationHeader.substring(7);
        if (!jwtUtils.validateJwtToken(token)) {
            return null;
        }
        return jwtUtils.getSessionIdFromJwtToken(token);
    }

    private Map<String, Object> toSessionView(AuthSession session) {
        Map<String, Object> row = new HashMap<>();
        row.put("sessionId", session.getId());
        row.put("createdAt", session.getCreatedAt());
        row.put("lastUsedAt", session.getLastUsedAt());
        row.put("expiresAt", session.getExpiresAt());
        row.put("ipAddress", session.getIpAddress());
        row.put("userAgent", session.getUserAgent());
        return row;
    }

    private String challengeMessage(MfaMethod method) {
        return switch (method) {
            case TOTP -> "Enter the code from your authenticator app.";
            case SMS -> "OTP sent to your phone.";
            case EMAIL -> "OTP sent to your email.";
        };
    }
}