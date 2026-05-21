package com.mediconnect.prescription.service;

import com.mediconnect.prescription.dto.request.DispenseRequest;
import com.mediconnect.prescription.dto.response.DispensationResponse;
import com.mediconnect.prescription.exception.BusinessException;
import com.mediconnect.prescription.exception.ResourceNotFoundException;
import com.mediconnect.prescription.mapper.DispensationMapper;
import com.mediconnect.prescription.model.Dispensation;
import com.mediconnect.prescription.model.Prescription;
import com.mediconnect.prescription.repository.DispensationRepository;
import com.mediconnect.prescription.repository.PrescriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DispensationService {
    private final DispensationRepository dispensationRepository;
    private final PrescriptionRepository prescriptionRepository;
    private final DispensationMapper dispensationMapper;
    private final KafkaProducerService kafkaProducerService;

    public DispensationResponse dispenseMedication(String prescriptionId, DispenseRequest request) {
        log.info("Dispensing medication for prescription {}", prescriptionId);

        Prescription prescription = prescriptionRepository.findById(prescriptionId)
                .orElseThrow(() -> new ResourceNotFoundException("Prescription", "id", prescriptionId));

        if (prescription.getStatus() != Prescription.PrescriptionStatus.SENT) {
            throw new BusinessException("Prescription must be sent to pharmacy first");
        }

        if (prescription.getExpiresAt() != null && prescription.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BusinessException("Prescription has expired");
        }

        List<Dispensation.DispensedItem> dispensedItems = request.getItems() != null
                ? request.getItems().stream()
                    .map(item -> Dispensation.DispensedItem.builder()
                            .drugName(item.getDrugName())
                            .quantityDispensed(item.getQuantityDispensed())
                            .batchNumber(item.getBatchNumber())
                            .build())
                    .toList()
                : null;

        Dispensation dispensation = Dispensation.builder()
                .prescriptionId(prescriptionId)
                .patientId(prescription.getPatientId())
                .pharmacyId(request.getPharmacyId())
                .pharmacistId(request.getPharmacistId())
                .dispensedAt(LocalDateTime.now())
                .items(dispensedItems)
                .status(Dispensation.DispensationStatus.COMPLETE)
                .notes(request.getNotes())
                .build();

        dispensation = dispensationRepository.save(dispensation);

        prescription.setStatus(Prescription.PrescriptionStatus.DISPENSED);
        prescription.setDispensedAt(LocalDateTime.now());
        prescription.setDispensedByPharmacistId(request.getPharmacistId());
        prescriptionRepository.save(prescription);

        kafkaProducerService.publishPrescriptionDispensed(prescriptionId, prescription.getPatientId(), request.getPharmacyId());

        log.info("Medication dispensed for prescription {}", prescriptionId);
        return dispensationMapper.toResponse(dispensation);
    }

    public List<DispensationResponse> getDispensations(String prescriptionId) {
        log.info("Fetching dispensations for prescription {}", prescriptionId);
        List<Dispensation> dispensations = dispensationRepository.findByPrescriptionId(prescriptionId);
        return dispensationMapper.toResponseList(dispensations);
    }
}
