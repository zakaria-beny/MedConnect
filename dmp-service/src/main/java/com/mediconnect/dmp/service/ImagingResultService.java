package com.mediconnect.dmp.service;

import com.mediconnect.dmp.dto.request.ImagingResultRequest;
import com.mediconnect.dmp.dto.response.ImagingResultResponse;
import com.mediconnect.dmp.exception.ResourceNotFoundException;
import com.mediconnect.dmp.mapper.ImagingResultMapper;
import com.mediconnect.dmp.model.ImagingResult;
import com.mediconnect.dmp.repository.ImagingResultRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImagingResultService {

    private final ImagingResultRepository imagingResultRepository;
    private final ImagingResultMapper imagingResultMapper;
    private final KafkaProducerService kafkaProducerService;

    public ImagingResultResponse addImagingStudy(String patientId, ImagingResultRequest request) {
        log.info("Adding imaging study {} for patient {}", request.getStudyType(), patientId);
        ImagingResult imagingResult = imagingResultMapper.toModel(request, patientId);
        ImagingResult saved = imagingResultRepository.save(imagingResult);
        kafkaProducerService.publishDmpUpdated(patientId, "imaging", "CREATED");
        log.info("Imaging study {} added successfully", saved.getId());
        return imagingResultMapper.toResponse(saved);
    }

    public List<ImagingResultResponse> getImagingResults(String patientId) {
        log.info("Fetching imaging results for patient {}", patientId);
        List<ImagingResult> imagingResults = imagingResultRepository.findByPatientIdOrderByStudyDateDesc(patientId);
        return imagingResultMapper.toResponseList(imagingResults);
    }

    public ImagingResultResponse getImagingResultById(String patientId, String imagingId) {
        log.info("Fetching imaging result {} for patient {}", imagingId, patientId);
        ImagingResult imagingResult = imagingResultRepository.findById(imagingId)
                .orElseThrow(() -> new ResourceNotFoundException("ImagingResult", "id", imagingId));
        
        if (!imagingResult.getPatientId().equals(patientId)) {
            throw new ResourceNotFoundException("ImagingResult", "id", imagingId);
        }
        
        return imagingResultMapper.toResponse(imagingResult);
    }

    public ImagingResultResponse updateImagingResult(String patientId, String imagingId, ImagingResultRequest request) {
        log.info("Updating imaging result {} for patient {}", imagingId, patientId);
        ImagingResult imagingResult = imagingResultRepository.findById(imagingId)
                .orElseThrow(() -> new ResourceNotFoundException("ImagingResult", "id", imagingId));
        
        if (!imagingResult.getPatientId().equals(patientId)) {
            throw new ResourceNotFoundException("ImagingResult", "id", imagingId);
        }
        
        imagingResult.setStudyType(request.getStudyType());
        imagingResult.setBodyPart(request.getBodyPart());
        imagingResult.setStudyDate(request.getStudyDate());
        imagingResult.setPerformedBy(request.getPerformedBy());
        imagingResult.setRadiologist(request.getRadiologist());
        imagingResult.setDicomPath(request.getDicomPath());
        imagingResult.setInterpretation(request.getInterpretation());
        imagingResult.setFindings(request.getFindings());
        imagingResult.setImpression(request.getImpression());
        imagingResult.setRecommendations(request.getRecommendations());
        imagingResult.setStatus(request.getStatus());
        
        ImagingResult updated = imagingResultRepository.save(imagingResult);
        kafkaProducerService.publishDmpUpdated(patientId, "imaging", "UPDATED");
        log.info("Imaging result {} updated successfully", imagingId);
        return imagingResultMapper.toResponse(updated);
    }

    public List<ImagingResultResponse> getImagingResultsByType(String patientId, String studyType) {
        log.info("Fetching {} imaging results for patient {}", studyType, patientId);
        List<ImagingResult> imagingResults = imagingResultRepository.findByPatientIdAndStudyType(patientId, studyType);
        return imagingResultMapper.toResponseList(imagingResults);
    }

    public List<ImagingResultResponse> getImagingResultsByDateRange(String patientId, LocalDateTime startDate, LocalDateTime endDate) {
        log.info("Fetching imaging results between {} and {} for patient {}", startDate, endDate, patientId);
        List<ImagingResult> imagingResults = imagingResultRepository.findByPatientIdAndStudyDateBetween(patientId, startDate, endDate);
        return imagingResultMapper.toResponseList(imagingResults);
    }
}
