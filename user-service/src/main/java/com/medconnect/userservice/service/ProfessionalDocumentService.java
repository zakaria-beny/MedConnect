package com.medconnect.userservice.service;

import com.medconnect.userservice.dto.ProfessionalDocumentResponse;
import com.medconnect.userservice.entity.ProfessionalDocument;
import com.medconnect.userservice.entity.ProfessionalDocumentScanStatus;
import com.medconnect.userservice.entity.ProfessionalDocumentSide;
import com.medconnect.userservice.entity.ProfessionalProfileType;
import com.medconnect.userservice.repository.ProfessionalDocumentRepository;
import com.medconnect.userservice.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

@Service
public class ProfessionalDocumentService {

    private static final long MB = 1024L * 1024L;
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(".jpg", ".jpeg", ".png", ".pdf");
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "application/pdf"
    );
    private static final String EICAR_TEST_SIGNATURE = "X5O!P%@AP[4\\PZX54(P^)7CC)7}$EICAR-STANDARD-ANTIVIRUS-TEST-FILE!$H+H*";
    private static final String ENCRYPTION_ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_TAG_LENGTH = 128;
    private static final int GCM_IV_BYTES = 12;

    private final ProfessionalDocumentRepository professionalDocumentRepository;
    private final UserRepository userRepository;
    private final ProfessionalVerificationAuditService auditService;
    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${medconnect.upload.professional-proof-dir:uploads/professional-proofs}")
    private String professionalProofDir;
    @Value("${medconnect.upload.max-size-bytes:5242880}")
    private long maxUploadSizeBytes;
    @Value("${medconnect.upload.signed-link-expiry-seconds:600}")
    private long signedLinkExpirySeconds;
    @Value("${medconnect.upload.signed-link-secret:}")
    private String signedLinkSecret;
    @Value("${medconnect.upload.encryption-key-base64:}")
    private String encryptionKeyBase64;
    @Value("${medconnect.app.jwtSecret}")
    private String jwtSecret;

    public ProfessionalDocumentService(
            ProfessionalDocumentRepository professionalDocumentRepository,
            UserRepository userRepository,
            ProfessionalVerificationAuditService auditService
    ) {
        this.professionalDocumentRepository = professionalDocumentRepository;
        this.userRepository = userRepository;
        this.auditService = auditService;
    }

    public ProfessionalDocumentResponse uploadDocument(
            String userId,
            String uploadedByUserId,
            ProfessionalProfileType profileType,
            ProfessionalDocumentSide side,
            MultipartFile file
    ) {
        if (!StringUtils.hasText(userId)) {
            throw new RuntimeException("userId is required.");
        }
        if (!userRepository.existsById(userId)) {
            throw new RuntimeException("User not found with id " + userId);
        }
        if (!StringUtils.hasText(uploadedByUserId)) {
            throw new RuntimeException("Authenticated user is required.");
        }
        if (profileType == null) {
            throw new RuntimeException("profileType is required.");
        }
        if (side == null) {
            throw new RuntimeException("side is required.");
        }
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("file is required.");
        }

        String originalFilename = StringUtils.hasText(file.getOriginalFilename())
                ? file.getOriginalFilename().trim()
                : "document";
        byte[] inputBytes;
        try {
            inputBytes = file.getBytes();
        } catch (IOException ex) {
            throw new RuntimeException("Unable to read uploaded file.", ex);
        }
        validateFile(file, originalFilename, inputBytes);
        ProfessionalDocumentScanStatus scanStatus = scanForMalware(inputBytes);
        if (scanStatus != ProfessionalDocumentScanStatus.CLEAN) {
            throw new RuntimeException("Uploaded file failed malware scan.");
        }

        String extension = extractExtension(originalFilename);
        String storedFilename = userId + "-" + profileType.name().toLowerCase() + "-" + side.name().toLowerCase()
                + "-" + UUID.randomUUID() + extension;

        Path root = Paths.get(professionalProofDir).toAbsolutePath().normalize();
        Path target = root.resolve(storedFilename).normalize();
        if (!target.startsWith(root)) {
            throw new RuntimeException("Invalid file path.");
        }

        List<ProfessionalDocument> activeForSide = professionalDocumentRepository
                .findByUserIdAndProfileTypeAndSideAndActiveTrue(userId, profileType, side);
        activeForSide.forEach(existing -> existing.setActive(false));
        if (!activeForSide.isEmpty()) {
            professionalDocumentRepository.saveAll(activeForSide);
        }

        int nextVersion = professionalDocumentRepository
                .findTopByUserIdAndProfileTypeAndSideOrderByVersionDesc(userId, profileType, side)
                .map(existing -> existing.getVersion() + 1)
                .orElse(1);

        byte[] encryptedBytes = encrypt(inputBytes);
        try {
            Files.createDirectories(root);
            Files.write(target, encryptedBytes);
        } catch (IOException ex) {
            throw new RuntimeException("Unable to store uploaded file.", ex);
        }

        ProfessionalDocument document = new ProfessionalDocument();
        document.setUserId(userId);
        document.setProfileType(profileType);
        document.setSide(side);
        document.setOriginalFilename(originalFilename);
        document.setStoredFilename(storedFilename);
        document.setStoragePath(target.toString());
        document.setContentType(StringUtils.hasText(file.getContentType()) ? file.getContentType() : "application/octet-stream");
        document.setSizeBytes(file.getSize());
        document.setVersion(nextVersion);
        document.setActive(true);
        document.setScanStatus(scanStatus);
        document.setScanDetail("clean");
        document.setScannedAt(LocalDateTime.now());
        document.setEncrypted(true);
        document.setEncryptionAlgorithm(ENCRYPTION_ALGORITHM);
        document.setUploadedByUserId(uploadedByUserId);
        document.setUploadedAt(LocalDateTime.now());

        ProfessionalDocument saved = professionalDocumentRepository.save(document);
        auditService.logDocumentUploaded(
                userId,
                profileType,
                uploadedByUserId,
                saved.getId(),
                "Uploaded " + side.name() + " proof document v" + saved.getVersion()
        );
        return toResponse(saved, null);
    }

    public List<ProfessionalDocumentResponse> getDocumentsByUser(String userId) {
        if (!StringUtils.hasText(userId)) {
            throw new RuntimeException("userId is required.");
        }
        return professionalDocumentRepository.findByUserIdOrderByUploadedAtDesc(userId).stream()
                .map(document -> toResponse(document, null))
                .toList();
    }

    public ProfessionalDocument getDocumentOrThrow(String documentId) {
        if (!StringUtils.hasText(documentId)) {
            throw new RuntimeException("documentId is required.");
        }
        return professionalDocumentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document not found."));
    }

    public Resource loadDocumentAsResource(String documentId) {
        ProfessionalDocument document = getDocumentOrThrow(documentId);
        try {
            Path path = Paths.get(document.getStoragePath()).toAbsolutePath().normalize();
            if (!Files.exists(path) || !Files.isReadable(path)) {
                throw new RuntimeException("Stored file is missing.");
            }
            byte[] encrypted = Files.readAllBytes(path);
            byte[] plain = decrypt(encrypted);
            return new ByteArrayResource(plain);
        } catch (Exception ex) {
            throw new RuntimeException("Unable to read stored document.", ex);
        }
    }

    public boolean hasActiveCleanDocument(String userId, ProfessionalProfileType profileType, ProfessionalDocumentSide side) {
        return professionalDocumentRepository.countByUserIdAndProfileTypeAndSideAndActiveTrueAndScanStatus(
                userId,
                profileType,
                side,
                ProfessionalDocumentScanStatus.CLEAN
        ) > 0;
    }

    public String generateSignedDownloadQuery(String documentId) {
        long exp = Instant.now().getEpochSecond() + Math.max(60, signedLinkExpirySeconds);
        String payload = documentId + ":" + exp;
        String signature = hmacSha256Base64Url(payload, resolveSignedLinkSecretBytes());
        return "exp=" + exp + "&sig=" + signature;
    }

    public void validateSignedDownload(String documentId, long exp, String sig) {
        long now = Instant.now().getEpochSecond();
        if (exp <= now) {
            throw new RuntimeException("Signed download link has expired.");
        }
        String payload = documentId + ":" + exp;
        String expected = hmacSha256Base64Url(payload, resolveSignedLinkSecretBytes());
        if (!MessageDigest.isEqual(expected.getBytes(), sig.getBytes())) {
            throw new RuntimeException("Invalid signed download link.");
        }
    }

    public static ProfessionalDocumentResponse toResponse(ProfessionalDocument document, String downloadUrl) {
        ProfessionalDocumentResponse response = new ProfessionalDocumentResponse();
        response.setId(document.getId());
        response.setUserId(document.getUserId());
        response.setProfileType(document.getProfileType());
        response.setSide(document.getSide());
        response.setOriginalFilename(document.getOriginalFilename());
        response.setContentType(document.getContentType());
        response.setSizeBytes(document.getSizeBytes());
        response.setVersion(document.getVersion());
        response.setActive(document.isActive());
        response.setScanStatus(document.getScanStatus());
        response.setUploadedAt(document.getUploadedAt());
        response.setDownloadUrl(downloadUrl);
        return response;
    }

    private static String extractExtension(String filename) {
        int index = filename.lastIndexOf('.');
        if (index < 0 || index == filename.length() - 1) {
            return "";
        }
        String ext = filename.substring(index).toLowerCase();
        return ext.matches("\\.[a-z0-9]{1,10}") ? ext : "";
    }

    private void validateFile(MultipartFile file, String originalFilename, byte[] bytes) {
        if (bytes.length == 0) {
            throw new RuntimeException("Uploaded file is empty.");
        }
        if (bytes.length > Math.max(1, maxUploadSizeBytes)) {
            throw new RuntimeException("Uploaded file exceeds max allowed size (" + (maxUploadSizeBytes / MB) + "MB).");
        }
        String extension = extractExtension(originalFilename);
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new RuntimeException("Unsupported file extension. Allowed: .jpg, .jpeg, .png, .pdf");
        }
        String contentType = StringUtils.hasText(file.getContentType()) ? file.getContentType().toLowerCase() : "";
        if (!ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new RuntimeException("Unsupported file type. Allowed: image/jpeg, image/png, application/pdf");
        }
    }

    private static ProfessionalDocumentScanStatus scanForMalware(byte[] bytes) {
        String body = new String(bytes);
        String normalized = body.replace("\\\\", "\\");
        if (body.contains(EICAR_TEST_SIGNATURE) || normalized.contains(EICAR_TEST_SIGNATURE)) {
            return ProfessionalDocumentScanStatus.INFECTED;
        }
        return ProfessionalDocumentScanStatus.CLEAN;
    }

    private byte[] encrypt(byte[] inputBytes) {
        try {
            byte[] iv = new byte[GCM_IV_BYTES];
            secureRandom.nextBytes(iv);
            Cipher cipher = Cipher.getInstance(ENCRYPTION_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, resolveEncryptionKey(), new GCMParameterSpec(GCM_TAG_LENGTH, iv));
            byte[] encrypted = cipher.doFinal(inputBytes);
            return ByteBuffer.allocate(iv.length + encrypted.length).put(iv).put(encrypted).array();
        } catch (Exception ex) {
            throw new RuntimeException("Unable to encrypt uploaded file.", ex);
        }
    }

    private byte[] decrypt(byte[] encryptedWithIv) {
        if (encryptedWithIv.length <= GCM_IV_BYTES) {
            throw new RuntimeException("Stored encrypted file is invalid.");
        }
        try {
            ByteBuffer buffer = ByteBuffer.wrap(encryptedWithIv);
            byte[] iv = new byte[GCM_IV_BYTES];
            buffer.get(iv);
            byte[] encrypted = new byte[buffer.remaining()];
            buffer.get(encrypted);
            Cipher cipher = Cipher.getInstance(ENCRYPTION_ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, resolveEncryptionKey(), new GCMParameterSpec(GCM_TAG_LENGTH, iv));
            return cipher.doFinal(encrypted);
        } catch (Exception ex) {
            throw new RuntimeException("Unable to decrypt stored document.", ex);
        }
    }

    private SecretKeySpec resolveEncryptionKey() {
        try {
            byte[] keyBytes;
            if (StringUtils.hasText(encryptionKeyBase64)) {
                keyBytes = Base64.getDecoder().decode(encryptionKeyBase64.trim());
                if (!(keyBytes.length == 16 || keyBytes.length == 24 || keyBytes.length == 32)) {
                    throw new RuntimeException("medconnect.upload.encryption-key-base64 must decode to 16/24/32 bytes.");
                }
                return new SecretKeySpec(keyBytes, "AES");
            }
            MessageDigest sha = MessageDigest.getInstance("SHA-256");
            keyBytes = sha.digest(jwtSecret.getBytes());
            return new SecretKeySpec(keyBytes, "AES");
        } catch (Exception ex) {
            throw new RuntimeException("Unable to initialize upload encryption key.", ex);
        }
    }

    private byte[] resolveSignedLinkSecretBytes() {
        String secret = StringUtils.hasText(signedLinkSecret) ? signedLinkSecret : jwtSecret;
        return secret.getBytes();
    }

    private static String hmacSha256Base64Url(String payload, byte[] keyBytes) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(keyBytes, "HmacSHA256"));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(mac.doFinal(payload.getBytes()));
        } catch (Exception ex) {
            throw new RuntimeException("Unable to generate signed link.", ex);
        }
    }
}
