package com.mediconnect.dmp.service;

import com.mediconnect.dmp.dto.request.ChronicConditionRequest;
import com.mediconnect.dmp.dto.response.ChronicConditionResponse;
import com.mediconnect.dmp.exception.ResourceNotFoundException;
import com.mediconnect.dmp.mapper.ChronicConditionMapper;
import com.mediconnect.dmp.model.ChronicCondition;
import com.mediconnect.dmp.repository.ChronicConditionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChronicConditionService {

    private final ChronicConditionRepository conditionRepository;
    private final ChronicConditionMapper conditionMapper;
    private final KafkaProducerService kafkaProducerService;

    public ChronicConditionResponse addCondition(String patientId, ChronicConditionRequest request) {
        log.info("Adding chronic condition '{}' for patient {}", request.getConditionName(), patientId);

        ChronicCondition condition = conditionMapper.toModel(request, patientId);
        ChronicCondition saved = conditionRepository.save(condition);

        kafkaProducerService.publishDmpUpdated(patientId, "chronic_conditions", "CREATED");

        return conditionMapper.toResponse(saved);
    }

    public List<ChronicConditionResponse> getConditions(String patientId) {
        log.info("Fetching chronic conditions for patient {}", patientId);
        List<ChronicCondition> conditions = conditionRepository.findByPatientId(patientId);
        return conditionMapper.toResponseList(conditions);
    }

    public ChronicConditionResponse getConditionById(String conditionId) {
        log.info("Fetching chronic condition with id {}", conditionId);
        ChronicCondition condition = conditionRepository.findById(conditionId)
                .orElseThrow(() -> new ResourceNotFoundException("ChronicCondition", "id", conditionId));
        return conditionMapper.toResponse(condition);
    }

    public ChronicConditionResponse updateCondition(String conditionId, ChronicConditionRequest request) {
        log.info("Updating chronic condition {}", conditionId);
        ChronicCondition existing = conditionRepository.findById(conditionId)
                .orElseThrow(() -> new ResourceNotFoundException("ChronicCondition", "id", conditionId));

        conditionMapper.updateModelFromRequest(existing, request);
        ChronicCondition updated = conditionRepository.save(existing);

        kafkaProducerService.publishDmpUpdated(existing.getPatientId(), "chronic_conditions", "UPDATED");

        return conditionMapper.toResponse(updated);
    }

    public void deleteCondition(String conditionId) {
        log.info("Deleting chronic condition {}", conditionId);
        if (!conditionRepository.existsById(conditionId)) {
            throw new ResourceNotFoundException("ChronicCondition", "id", conditionId);
        }
        conditionRepository.deleteById(conditionId);
    }
}
