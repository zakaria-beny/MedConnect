package com.mediconnect.messaging.service;

import com.mediconnect.messaging.document.EncryptionKey;
import com.mediconnect.messaging.repository.EncryptionKeyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class EncryptionService {

    private final EncryptionKeyRepository encryptionKeyRepository;

    private static final String AES = "AES";
    private static final String AES_GCM = "AES/GCM/NoPadding";
    private static final int GCM_TAG_LENGTH = 128;
    private static final int GCM_IV_LENGTH = 12;
    private static final int AES_KEY_SIZE = 256;

    public KeyPair generateKeyPair() {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("EC");
            keyPairGenerator.initialize(256);
            return keyPairGenerator.generateKeyPair();
        } catch (Exception e) {
            throw new RuntimeException("Erreur génération clé", e);
        }
    }

    public EncryptionKey initializeConversation(String conversationId, String userId) {
        try {
            KeyPair keyPair = generateKeyPair();
            String publicKey = Base64.getEncoder()
                    .encodeToString(keyPair.getPublic().getEncoded());
            String privateKey = Base64.getEncoder()
                    .encodeToString(keyPair.getPrivate().getEncoded());

            EncryptionKey encKey = new EncryptionKey();
            encKey.setConversationId(conversationId);
            encKey.setUserId(userId);
            encKey.setPublicKey(publicKey);
            encKey.setEncryptedPrivateKey(privateKey);
            encKey.setCreatedAt(LocalDateTime.now());

            return encryptionKeyRepository.save(encKey);
        } catch (Exception e) {
            throw new RuntimeException("Erreur initialisation conversation", e);
        }
    }

    public String encryptMessage(String content) {
        try {
            KeyGenerator keyGen = KeyGenerator.getInstance(AES);
            keyGen.init(AES_KEY_SIZE);
            SecretKey secretKey = keyGen.generateKey();

            byte[] iv = new byte[GCM_IV_LENGTH];
            new SecureRandom().nextBytes(iv);

            Cipher cipher = Cipher.getInstance(AES_GCM);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, parameterSpec);

            byte[] encryptedContent = cipher.doFinal(content.getBytes());

            byte[] combined = new byte[iv.length + encryptedContent.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(encryptedContent, 0, combined, iv.length, encryptedContent.length);

            return Base64.getEncoder().encodeToString(combined) + ":" +
                    Base64.getEncoder().encodeToString(secretKey.getEncoded());

        } catch (Exception e) {
            throw new RuntimeException("Erreur chiffrement", e);
        }
    }

    public String decryptMessage(String encryptedData) {
        try {
            String[] parts = encryptedData.split(":");
            byte[] combined = Base64.getDecoder().decode(parts[0]);
            byte[] keyBytes = Base64.getDecoder().decode(parts[1]);

            byte[] iv = new byte[GCM_IV_LENGTH];
            byte[] encrypted = new byte[combined.length - GCM_IV_LENGTH];
            System.arraycopy(combined, 0, iv, 0, iv.length);
            System.arraycopy(combined, iv.length, encrypted, 0, encrypted.length);

            SecretKey secretKey = new SecretKeySpec(keyBytes, AES);
            Cipher cipher = Cipher.getInstance(AES_GCM);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, parameterSpec);

            return new String(cipher.doFinal(encrypted));

        } catch (Exception e) {
            throw new RuntimeException("Erreur déchiffrement", e);
        }
    }

    public EncryptionKey rotateKeys(String conversationId, String userId) {
        EncryptionKey existing = encryptionKeyRepository
                .findByConversationIdAndUserId(conversationId, userId)
                .orElseThrow(() -> new RuntimeException("Clé non trouvée"));

        KeyPair newKeyPair = generateKeyPair();
        existing.setPublicKey(Base64.getEncoder()
                .encodeToString(newKeyPair.getPublic().getEncoded()));
        existing.setEncryptedPrivateKey(Base64.getEncoder()
                .encodeToString(newKeyPair.getPrivate().getEncoded()));
        existing.setRotatedAt(LocalDateTime.now());

        return encryptionKeyRepository.save(existing);
    }

    public boolean verifySignature(String messageId) {
        return true;
    }
}