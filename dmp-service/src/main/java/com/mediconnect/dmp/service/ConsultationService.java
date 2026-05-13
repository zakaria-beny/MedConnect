package com.mediconnect.dmp.service;

import com.mediconnect.dmp.dto.request.ConsultationRequest;
import com.mediconnect.dmp.dto.response.ConsultationResponse;
import com.mediconnect.dmp.exception.ResourceNotFoundException;
import com.mediconnect.dmp.mapper.ConsultationMapper;
import com.mediconnect.dmp.model.Consultation;
import com.mediconnect.dmp.repository.ConsultationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConsultationService {

    private final ConsultationRepository consultationRepository;
    private final ConsultationMapper consultationMapper;
    private final KafkaProducerService kafkaProducerService;

    public ConsultationResponse addConsultation(String patientId, ConsultationRequest request) {
        log.info("Adding consultation for patient {} with doctor {}", patientId, request.getDoctorId());

        Consultation consultation = consultationMapper.toModel(request, patientId);
        Consultation saved = consultationRepository.save(consultation);

        kafkaProducerService.publishDmpUpdated(patientId, "consultations", "CREATED");

        return consultationMapper.toResponse(saved);
    }

    public List<ConsultationResponse> getConsultationHistory(String patientId) {
        log.info("Fetching consultation history for patient {}", patientId);
        List<Consultation> consultations = consultationRepository.findByPatientIdOrderByConsultationDateDesc(patientId);
        return consultationMapper.toResponseList(consultations);
    }

    public ConsultationResponse getConsultationById(String consultationId) {
        log.info("Fetching consultation with id {}", consultationId);
        Consultation consultation = consultationRepository.findById(consultationId)
                .orElseThrow(() -> new ResourceNotFoundException("Consultation", "id", consultationId));
        return consultationMapper.toResponse(consultation);
    }
}
