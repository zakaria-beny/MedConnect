package com.mediconnect.dmp.service;

import com.mediconnect.dmp.dto.request.MedicationRequest;
import com.mediconnect.dmp.dto.response.MedicationResponse;
import com.mediconnect.dmp.exception.ResourceNotFoundException;
import com.mediconnect.dmp.mapper.MedicationMapper;
import com.mediconnect.dmp.model.Medication;
import com.mediconnect.dmp.repository.MedicationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MedicationService {

    private final MedicationRepository medicationRepository;
    private final MedicationMapper medicationMapper;
    private final KafkaProducerService kafkaProducerService;

    public MedicationResponse addMedication(String patientId, MedicationRequest request) {
        log.info("Adding medication '{}' for patient {}", request.getDrugName(), patientId);

        Medication medication = medicationMapper.toModel(request, patientId);
        Medication saved = medicationRepository.save(medication);

        kafkaProducerService.publishDmpUpdated(patientId, "medications", "CREATED");

        return medicationMapper.toResponse(saved);
    }

    public List<MedicationResponse> getCurrentMedications(String patientId) {
        log.info("Fetching current medications for patient {}", patientId);
        List<Medication> medications = medicationRepository.findByPatientIdAndStatus(patientId, Medication.MedicationStatus.ACTIVE);
        return medicationMapper.toResponseList(medications);
    }

    public List<MedicationResponse> getAllMedications(String patientId) {
        log.info("Fetching all medications for patient {}", patientId);
        List<Medication> medications = medicationRepository.findByPatientId(patientId);
        return medicationMapper.toResponseList(medications);
    }

    public MedicationResponse getMedicationById(String medicationId) {
        log.info("Fetching medication with id {}", medicationId);
        Medication medication = medicationRepository.findById(medicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Medication", "id", medicationId));
        return medicationMapper.toResponse(medication);
    }

    public MedicationResponse updateMedication(String medicationId, MedicationRequest request) {
        log.info("Updating medication {}", medicationId);
        Medication existing = medicationRepository.findById(medicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Medication", "id", medicationId));

        medicationMapper.updateModelFromRequest(existing, request);
        Medication updated = medicationRepository.save(existing);

        kafkaProducerService.publishDmpUpdated(existing.getPatientId(), "medications", "UPDATED");

        return medicationMapper.toResponse(updated);
    }

    public MedicationResponse stopMedication(String medicationId) {
        log.info("Stopping medication {}", medicationId);
        Medication existing = medicationRepository.findById(medicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Medication", "id", medicationId));

        existing.setStatus(Medication.MedicationStatus.STOPPED);
        Medication updated = medicationRepository.save(existing);

        kafkaProducerService.publishDmpUpdated(existing.getPatientId(), "medications", "STOPPED");

        return medicationMapper.toResponse(updated);
    }
}
