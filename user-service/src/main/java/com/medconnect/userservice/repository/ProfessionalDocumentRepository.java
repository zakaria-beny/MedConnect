package com.medconnect.userservice.repository;

import com.medconnect.userservice.entity.ProfessionalDocument;
import com.medconnect.userservice.entity.ProfessionalDocumentSide;
import com.medconnect.userservice.entity.ProfessionalProfileType;
import com.medconnect.userservice.entity.ProfessionalDocumentScanStatus;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface ProfessionalDocumentRepository extends MongoRepository<ProfessionalDocument, String> {
    List<ProfessionalDocument> findByUserIdOrderByUploadedAtDesc(String userId);
    List<ProfessionalDocument> findByUserIdAndProfileTypeOrderByUploadedAtDesc(String userId, ProfessionalProfileType profileType);
    List<ProfessionalDocument> findByUserIdAndProfileTypeAndActiveTrueOrderByUploadedAtDesc(String userId, ProfessionalProfileType profileType);
    Optional<ProfessionalDocument> findTopByUserIdAndProfileTypeAndSideAndActiveTrueOrderByVersionDesc(
            String userId,
            ProfessionalProfileType profileType,
            ProfessionalDocumentSide side
    );
    Optional<ProfessionalDocument> findTopByUserIdAndProfileTypeAndSideOrderByVersionDesc(
            String userId,
            ProfessionalProfileType profileType,
            ProfessionalDocumentSide side
    );
    List<ProfessionalDocument> findByUserIdAndProfileTypeAndSideAndActiveTrue(
            String userId,
            ProfessionalProfileType profileType,
            ProfessionalDocumentSide side
    );
    long countByUserIdAndProfileTypeAndSideAndActiveTrueAndScanStatus(
            String userId,
            ProfessionalProfileType profileType,
            ProfessionalDocumentSide side,
            ProfessionalDocumentScanStatus scanStatus
    );
}
