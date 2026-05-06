package com.medconnect.userservice.otp;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
public class OtpService {

    private final OtpRepository otpRepository;
    private final MongoTemplate mongoTemplate;
    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${medconnect.otp.expiry-minutes:10}")
    private int expiryMinutes;

    public OtpService(OtpRepository otpRepository, MongoTemplate mongoTemplate) {
        this.otpRepository = otpRepository;
        this.mongoTemplate = mongoTemplate;
    }

    /**
     * Generates a new 6-digit OTP, invalidates any existing unused OTPs of the same type,
     * persists it, and returns the code so the caller can email it.
     */
    public String generateAndSave(String email, OtpType type) {
        // Invalidate previous OTPs for this email+type
        otpRepository.deleteAllByEmailAndType(email, type);

        String code = String.format("%06d", secureRandom.nextInt(1_000_000));

        OtpRecord record = new OtpRecord();
        record.setEmail(email);
        record.setCode(code);
        record.setType(type);
        record.setCreatedAt(LocalDateTime.now());
        record.setExpiresAt(LocalDateTime.now().plusMinutes(expiryMinutes));
        record.setUsed(false);

        otpRepository.save(record);
        return code;
    }

    /**
     * Validates the OTP. Returns true and marks it as used if valid;
     * returns false if not found, expired, or already used.
     */
    public boolean validate(String email, OtpType type, String code) {
        LocalDateTime now = LocalDateTime.now();
        return otpRepository
                .findTopByEmailAndTypeAndUsedFalseOrderByCreatedAtDesc(email, type)
                .filter(r -> !r.getExpiresAt().isBefore(now))
                .filter(r -> r.getCode().equals(code))
                .map(r -> consumeOtp(r.getId(), code, now))
                .orElse(false);
    }

    private boolean consumeOtp(String otpId, String code, LocalDateTime now) {
        Query query = new Query();
        query.addCriteria(Criteria.where("_id").is(otpId));
        query.addCriteria(Criteria.where("used").is(false));
        query.addCriteria(Criteria.where("code").is(code));
        query.addCriteria(Criteria.where("expiresAt").gte(now));

        Update update = new Update().set("used", true);
        return mongoTemplate.updateFirst(query, update, OtpRecord.class).getModifiedCount() == 1;
    }
}
