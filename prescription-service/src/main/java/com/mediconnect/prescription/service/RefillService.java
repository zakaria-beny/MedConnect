package com.mediconnect.prescription.service;

import com.mediconnect.prescription.dto.request.RefillRequest;
import com.mediconnect.prescription.dto.response.RefillHistoryResponse;
import com.mediconnect.prescription.exception.BusinessException;
import com.mediconnect.prescription.exception.ResourceNotFoundException;
import com.mediconnect.prescription.mapper.RefillHistoryMapper;
import com.mediconnect.prescription.model.Prescription;
import com.mediconnect.prescription.model.RefillHistory;
import com.mediconnect.prescription.repository.PrescriptionRepository;
import com.mediconnect.prescription.repository.RefillHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RefillService {
    private final RefillHistoryRepository refillHistoryRepository;
    private final PrescriptionRepository prescriptionRepository;
    private final RefillHistoryMapper refillHistoryMapper;
    private final KafkaProducerService kafkaProducerService;

    public RefillHistoryResponse requestRefill(String prescriptionId, RefillRequest request) {
        log.info("Requesting refill for prescription {}", prescriptionId);

        Prescription prescription = prescriptionRepository.findById(prescriptionId)
                .orElseThrow(() -> new ResourceNotFoundException("Prescription", "id", prescriptionId));

        if (prescription.getRefillsUsed() >= prescription.getRefillsAllowed()) {
            throw new BusinessException("No refills remaining");
        }

        RefillHistory refillHistory = RefillHistory.builder()
                .originalPrescriptionId(prescriptionId)
                .patientId(request.getPatientId())
                .requestedByPatientId(request.getPatientId())
                .status(RefillHistory.RefillStatus.PENDING)
                .reason(request.getReason())
                .build();

        refillHistory = refillHistoryRepository.save(refillHistory);
        log.info("Refill requested for prescription {}", prescriptionId);
        return refillHistoryMapper.toResponse(refillHistory);
    }

    public RefillHistoryResponse approveRefill(String refillId, String doctorId) {
        log.info("Approving refill {}", refillId);

        RefillHistory refillHistory = refillHistoryRepository.findById(refillId)
                .orElseThrow(() -> new ResourceNotFoundException("Refill History", "id", refillId));

        refillHistory.setStatus(RefillHistory.RefillStatus.APPROVED);
        refillHistory.setApprovedByDoctorId(doctorId);
        refillHistory.setProcessedAt(LocalDateTime.now());

        String originalPrescriptionId = refillHistory.getOriginalPrescriptionId();
        Prescription prescription = prescriptionRepository.findById(originalPrescriptionId)
                .orElseThrow(() -> new ResourceNotFoundException("Prescription", "id", originalPrescriptionId));

        prescription.setRefillsUsed(prescription.getRefillsUsed() + 1);
        prescriptionRepository.save(prescription);

        refillHistory = refillHistoryRepository.save(refillHistory);
        kafkaProducerService.publishPrescriptionRefilled(refillHistory.getOriginalPrescriptionId(), refillHistory.getPatientId());

        log.info("Refill {} approved", refillId);
        return refillHistoryMapper.toResponse(refillHistory);
    }

    public RefillHistoryResponse rejectRefill(String refillId, String reason) {
        log.info("Rejecting refill {}", refillId);

        RefillHistory refillHistory = refillHistoryRepository.findById(refillId)
                .orElseThrow(() -> new ResourceNotFoundException("Refill History", "id", refillId));

        refillHistory.setStatus(RefillHistory.RefillStatus.REJECTED);
        refillHistory.setReason(reason);
        refillHistory.setProcessedAt(LocalDateTime.now());

        refillHistory = refillHistoryRepository.save(refillHistory);
        log.info("Refill {} rejected", refillId);
        return refillHistoryMapper.toResponse(refillHistory);
    }

    public List<RefillHistoryResponse> getRefillHistory(String prescriptionId) {
        log.info("Fetching refill history for prescription {}", prescriptionId);
        List<RefillHistory> refillHistories = refillHistoryRepository.findByOriginalPrescriptionId(prescriptionId);
        return refillHistoryMapper.toResponseList(refillHistories);
    }
}
