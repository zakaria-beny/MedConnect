package com.mediconnect.dmp.service;

import com.mediconnect.dmp.dto.request.ConsentRequest;
import com.mediconnect.dmp.dto.response.ConsentResponse;
import com.mediconnect.dmp.exception.ResourceNotFoundException;
import com.mediconnect.dmp.mapper.ConsentMapper;
import com.mediconnect.dmp.model.Consent;
import com.mediconnect.dmp.repository.ConsentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConsentService {

    private final ConsentRepository consentRepository;
    private final ConsentMapper consentMapper;

    public ConsentResponse grantAccess(String patientId, ConsentRequest request) {
        log.info("Granting access to patient {} for doctor {}", patientId, request.getDoctorId());

        Optional<Consent> existingConsent = consentRepository.findByPatientIdAndDoctorIdAndActiveTrue(patientId, request.getDoctorId());
        if (existingConsent.isPresent()) {
            Consent old = existingConsent.get();
            old.setActive(false);
            old.setRevokedAt(LocalDateTime.now());
            old.setRevocationReason("Replaced");
            consentRepository.save(old);
        }

        Consent newConsent = consentMapper.toModel(request, patientId);
        Consent saved = consentRepository.save(newConsent);

        return consentMapper.toResponse(saved);
    }

    public void revokeAccess(String patientId, String doctorId, String reason) {
        log.info("Revoking access for patient {} from doctor {}", patientId, doctorId);

        Consent consent = consentRepository.findByPatientIdAndDoctorIdAndActiveTrue(patientId, doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Consent", "doctorId", doctorId));

        consent.setActive(false);
        consent.setRevokedAt(LocalDateTime.now());
        consent.setRevocationReason(reason);
        consentRepository.save(consent);
    }

    public List<ConsentResponse> getConsents(String patientId) {
        log.info("Fetching active consents for patient {}", patientId);
        List<Consent> consents = consentRepository.findByPatientIdAndActiveTrue(patientId);
        return consentMapper.toResponseList(consents);
    }

    public boolean verifyAccess(String patientId, String doctorId) {
        log.info("Verifying access for patient {} from doctor {}", patientId, doctorId);

        Optional<Consent> consentOpt = consentRepository.findByPatientIdAndDoctorIdAndActiveTrue(patientId, doctorId);

        if (consentOpt.isEmpty()) {
            return false;
        }

        Consent consent = consentOpt.get();

        if (consent.getExpiresAt() != null && consent.getExpiresAt().isBefore(LocalDateTime.now())) {
            consent.setActive(false);
            consent.setRevokedAt(LocalDateTime.now());
            consent.setRevocationReason("Expired");
            consentRepository.save(consent);
            log.info("Consent expired for patient {} from doctor {}", patientId, doctorId);
            return false;
        }

        return true;
    }
}
