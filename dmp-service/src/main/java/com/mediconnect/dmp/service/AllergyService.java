package com.mediconnect.dmp.service;

import com.mediconnect.dmp.dto.request.AllergyRequest;
import com.mediconnect.dmp.dto.response.AllergyResponse;
import com.mediconnect.dmp.exception.DuplicateResourceException;
import com.mediconnect.dmp.exception.ResourceNotFoundException;
import com.mediconnect.dmp.mapper.AllergyMapper;
import com.mediconnect.dmp.model.Allergy;
import com.mediconnect.dmp.repository.AllergyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AllergyService {

    private final AllergyRepository allergyRepository;
    private final AllergyMapper allergyMapper;
    private final KafkaProducerService kafkaProducerService;

    public AllergyResponse addAllergy(String patientId, AllergyRequest request) {
        log.info("Adding allergy '{}' for patient {}", request.getAllergen(), patientId);

        if (allergyRepository.existsByPatientIdAndAllergen(patientId, request.getAllergen())) {
            throw new DuplicateResourceException(
                    String.format("Allergy '%s' already exists for patient %s", request.getAllergen(), patientId)
            );
        }

        Allergy allergy = allergyMapper.toModel(request, patientId);
        Allergy saved = allergyRepository.save(allergy);

        kafkaProducerService.publishAllergyAlert(patientId, request.getAllergen(), request.getSeverity().toString());
        kafkaProducerService.publishDmpUpdated(patientId, "allergies", "CREATED");

        return allergyMapper.toResponse(saved);
    }

    public List<AllergyResponse> getAllergies(String patientId) {
        log.info("Fetching all allergies for patient {}", patientId);
        List<Allergy> allergies = allergyRepository.findByPatientIdAndActiveTrue(patientId);
        return allergyMapper.toResponseList(allergies);
    }

    public AllergyResponse getAllergyById(String allergyId) {
        log.info("Fetching allergy with id {}", allergyId);
        Allergy allergy = allergyRepository.findById(allergyId)
                .orElseThrow(() -> new ResourceNotFoundException("Allergy", "id", allergyId));
        return allergyMapper.toResponse(allergy);
    }

    public AllergyResponse updateAllergy(String allergyId, AllergyRequest request) {
        log.info("Updating allergy {}", allergyId);
        Allergy existing = allergyRepository.findById(allergyId)
                .orElseThrow(() -> new ResourceNotFoundException("Allergy", "id", allergyId));

        allergyMapper.updateModelFromRequest(existing, request);
        Allergy updated = allergyRepository.save(existing);

        kafkaProducerService.publishDmpUpdated(existing.getPatientId(), "allergies", "UPDATED");

        return allergyMapper.toResponse(updated);
    }

    public void deleteAllergy(String allergyId) {
        log.info("Deleting allergy {}", allergyId);
        Allergy existing = allergyRepository.findById(allergyId)
                .orElseThrow(() -> new ResourceNotFoundException("Allergy", "id", allergyId));

        existing.setActive(false);
        allergyRepository.save(existing);

        kafkaProducerService.publishDmpUpdated(existing.getPatientId(), "allergies", "DELETED");
    }

    public boolean checkAllergy(String patientId, String allergen) {
        log.info("Checking if patient {} has allergy to {}", patientId, allergen);
        return allergyRepository.existsByPatientIdAndAllergenAndActiveTrue(patientId, allergen);
    }
}
