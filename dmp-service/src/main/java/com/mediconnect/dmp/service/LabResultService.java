package com.mediconnect.dmp.service;

import com.mediconnect.dmp.dto.request.LabResultRequest;
import com.mediconnect.dmp.dto.response.LabResultResponse;
import com.mediconnect.dmp.exception.ResourceNotFoundException;
import com.mediconnect.dmp.mapper.LabResultMapper;
import com.mediconnect.dmp.model.LabResult;
import com.mediconnect.dmp.repository.LabResultRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class LabResultService {

    private final LabResultRepository labResultRepository;
    private final LabResultMapper labResultMapper;
    private final KafkaProducerService kafkaProducerService;

    public LabResultResponse addLabResult(String patientId, LabResultRequest request) {
        log.info("Adding lab result '{}' for patient {}", request.getTestName(), patientId);

        LabResult labResult = labResultMapper.toModel(request, patientId);
        LabResult saved = labResultRepository.save(labResult);

        // Notify audit service that DMP was updated
        kafkaProducerService.publishDmpUpdated(patientId, "lab_results", "CREATED");

        return labResultMapper.toResponse(saved);
    }

    public List<LabResultResponse> getLabResults(String patientId) {
        log.info("Fetching all lab results for patient {}", patientId);
        List<LabResult> results = labResultRepository.findByPatientIdOrderByResultDateDesc(patientId);
        return labResultMapper.toResponseList(results);
    }

    public List<LabResultResponse> getLabResultsByCategory(String patientId, LabResult.LabCategory category) {
        List<LabResult> results = labResultRepository.findByPatientIdAndCategory(patientId, category);
        return labResultMapper.toResponseList(results);
    }

    public LabResultResponse getLabResultById(String labResultId) {
        LabResult labResult = labResultRepository.findById(labResultId)
                .orElseThrow(() -> new ResourceNotFoundException("LabResult", "id", labResultId));
        return labResultMapper.toResponse(labResult);
    }

    public LabResultResponse updateLabResult(String labResultId, LabResultRequest request) {
        LabResult existing = labResultRepository.findById(labResultId)
                .orElseThrow(() -> new ResourceNotFoundException("LabResult", "id", labResultId));

        existing.setTestName(request.getTestName());
        existing.setCategory(request.getCategory());
        existing.setValues(request.getValues());
        existing.setInterpretation(request.getInterpretation());
        existing.setStatus(request.getStatus());
        existing.setReportFilePath(request.getReportFilePath());

        LabResult updated = labResultRepository.save(existing);
        return labResultMapper.toResponse(updated);
    }
}