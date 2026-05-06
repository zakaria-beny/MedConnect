package com.medconnect.userservice.service;

import com.medconnect.userservice.dto.BulkImportResponse;
import com.medconnect.userservice.entity.BulkImport;
import com.medconnect.userservice.entity.BulkImportStatus;
import com.medconnect.userservice.entity.User;
import com.medconnect.userservice.googleAuth.Provider;
import com.medconnect.userservice.otp.EmailService;
import com.medconnect.userservice.repository.BulkImportRepository;
import com.medconnect.userservice.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class BulkImportService {

    private final BulkImportRepository bulkImportRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final UserManagementEventPublisher eventPublisher;
    private final SubscriptionPlanPolicyService subscriptionPlanPolicyService;

    public BulkImportService(
            BulkImportRepository bulkImportRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            EmailService emailService,
            UserManagementEventPublisher eventPublisher,
            SubscriptionPlanPolicyService subscriptionPlanPolicyService
    ) {
        this.bulkImportRepository = bulkImportRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.eventPublisher = eventPublisher;
        this.subscriptionPlanPolicyService = subscriptionPlanPolicyService;
    }

    public BulkImportResponse processBulkImport(String userId, MultipartFile file) {
        if (!StringUtils.hasText(userId)) {
            throw new RuntimeException("Authenticated user is required.");
        }
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("CSV file is required.");
        }
        if (!userRepository.existsById(userId)) {
            throw new RuntimeException("User not found with id " + userId);
        }

        LocalDateTime now = LocalDateTime.now();
        BulkImport bulkImport = new BulkImport();
        bulkImport.setUserId(userId);
        bulkImport.setFileName(file.getOriginalFilename());
        bulkImport.setStatus(BulkImportStatus.PROCESSING);
        bulkImport.setCreatedAt(now);
        bulkImport.setUpdatedAt(now);
        bulkImport = bulkImportRepository.save(bulkImport);

        long existingManagedUsers = userRepository.countByCreatedByUserId(userId);
        int maxPatientsForPlan = subscriptionPlanPolicyService.getLimitsForUser(userId).maxPatients();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8)
        )) {
            String line;
            int lineNumber = 0;
            while ((line = reader.readLine()) != null) {
                lineNumber++;

                if (!StringUtils.hasText(line)) {
                    continue;
                }
                if (lineNumber == 1 && looksLikeHeader(line)) {
                    continue;
                }

                bulkImport.setTotalRows(bulkImport.getTotalRows() + 1);
                if (existingManagedUsers + bulkImport.getSuccessCount() >= maxPatientsForPlan) {
                    bulkImport.setFailedCount(bulkImport.getFailedCount() + 1);
                    List<String> errors = bulkImport.getErrors() == null ? new ArrayList<>() : bulkImport.getErrors();
                    errors.add("Line " + lineNumber + ": plan patient limit reached (" + maxPatientsForPlan + ").");
                    bulkImport.setErrors(errors);
                    continue;
                }
                try {
                    createUserFromCsvLine(userId, line);
                    bulkImport.setSuccessCount(bulkImport.getSuccessCount() + 1);
                } catch (IllegalArgumentException ex) {
                    bulkImport.setFailedCount(bulkImport.getFailedCount() + 1);
                    List<String> errors = bulkImport.getErrors() == null ? new ArrayList<>() : bulkImport.getErrors();
                    errors.add("Line " + lineNumber + ": " + ex.getMessage());
                    bulkImport.setErrors(errors);
                }
            }
        } catch (IOException ex) {
            bulkImport.setStatus(BulkImportStatus.FAILED);
            bulkImport.setUpdatedAt(LocalDateTime.now());
            List<String> errors = bulkImport.getErrors() == null ? new ArrayList<>() : bulkImport.getErrors();
            errors.add("Unable to read CSV file.");
            bulkImport.setErrors(errors);
            bulkImportRepository.save(bulkImport);
            throw new RuntimeException("Unable to read CSV file.", ex);
        }

        bulkImport.setStatus(BulkImportStatus.COMPLETED);
        bulkImport.setUpdatedAt(LocalDateTime.now());
        return toResponse(bulkImportRepository.save(bulkImport));
    }

    public BulkImportResponse getImportStatus(String importId, String requesterUserId, boolean isAdmin) {
        BulkImport bulkImport = isAdmin
                ? bulkImportRepository.findById(importId).orElseThrow(() -> new RuntimeException("Import not found."))
                : bulkImportRepository.findByIdAndUserId(importId, requesterUserId)
                .orElseThrow(() -> new RuntimeException("Import not found."));

        return toResponse(bulkImport);
    }

    private void createUserFromCsvLine(String creatorUserId, String line) {
        String[] columns = parseCsv(line);
        if (columns.length < 3) {
            throw new IllegalArgumentException("Expected columns: nom, prenom, email, [telephone], [role]");
        }

        String nom = columns[0].trim();
        String prenom = columns[1].trim();
        String email = columns[2].trim().toLowerCase();
        String telephone = columns.length >= 4 ? columns[3].trim() : null;
        String rawRole = columns.length >= 5 ? columns[4].trim() : null;

        if (!StringUtils.hasText(nom) || !StringUtils.hasText(prenom) || !StringUtils.hasText(email)) {
            throw new IllegalArgumentException("nom, prenom, and email are required.");
        }
        if (!email.contains("@")) {
            throw new IllegalArgumentException("Invalid email format.");
        }
        if (userRepository.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("Email already exists: " + email);
        }

        String temporaryPassword = generateTemporaryPassword();
        User user = new User();
        user.setNom(nom);
        user.setPrenom(prenom);
        user.setEmail(email);
        user.setTelephone(StringUtils.hasText(telephone) ? telephone : null);
        user.setMotDePasse(passwordEncoder.encode(temporaryPassword));
        user.setRoles(List.of(normalizeRole(rawRole)));
        user.setProvider(Provider.LOCAL);
        user.setEnabled(true);
        user.setStatut("INVITED");
        user.setCreatedByUserId(creatorUserId);

        User saved = userRepository.save(user);
        emailService.sendInvitationEmail(saved.getEmail(), temporaryPassword);
        eventPublisher.publishUserCreated(saved.getId(), saved.getEmail(), "bulk_import");
    }

    private static String normalizeRole(String rawRole) {
        if (!StringUtils.hasText(rawRole)) {
            return "ROLE_USER";
        }
        String normalized = rawRole.trim().toUpperCase();
        return normalized.startsWith("ROLE_") ? normalized : "ROLE_" + normalized;
    }

    private static String generateTemporaryPassword() {
        return "Tmp-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
    }

    private static boolean looksLikeHeader(String line) {
        String lowered = line.toLowerCase();
        return lowered.contains("email") && (lowered.contains("nom") || lowered.contains("prenom"));
    }

    private static String[] parseCsv(String line) {
        List<String> values = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                inQuotes = !inQuotes;
                continue;
            }
            if (c == ',' && !inQuotes) {
                values.add(current.toString());
                current.setLength(0);
                continue;
            }
            current.append(c);
        }
        values.add(current.toString());
        return values.toArray(new String[0]);
    }

    private static BulkImportResponse toResponse(BulkImport bulkImport) {
        BulkImportResponse response = new BulkImportResponse();
        response.setId(bulkImport.getId());
        response.setUserId(bulkImport.getUserId());
        response.setFileName(bulkImport.getFileName());
        response.setStatus(bulkImport.getStatus() != null ? bulkImport.getStatus().name() : null);
        response.setTotalRows(bulkImport.getTotalRows());
        response.setSuccessCount(bulkImport.getSuccessCount());
        response.setFailedCount(bulkImport.getFailedCount());
        response.setErrors(bulkImport.getErrors());
        return response;
    }
}
